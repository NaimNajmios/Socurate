package com.najmi.oreamnos;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import io.noties.markwon.Markwon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.najmi.oreamnos.curator.CuratorFactory;
import com.najmi.oreamnos.model.GenerationPill;
import com.najmi.oreamnos.services.ContentGenerationService;
import com.najmi.oreamnos.utils.NotificationHelper;
import com.najmi.oreamnos.utils.PreferencesManager;
import com.najmi.oreamnos.viewmodel.GenerationState;
import com.najmi.oreamnos.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for the Oreamnos app with Quick Edit and Hashtag Manager.
 * Allows users to input text/URLs, generate posts, edit them, and manage
 * hashtags.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextInputEditText inputText;
    private TextInputEditText outputText;
    private TextView editedIndicator;
    private TextView progressText;
    private TextView inputCharCount;
    private TextView outputWordCount;
    private MaterialCardView outputCard;
    private MaterialCardView skeletonCard;
    private View progressOverlay;
    private View placeholderView;
    private ImageButton clearInputButton;
    private ImageButton resetAllButton;
    private ImageButton pasteButton;
    private MaterialButton editButton;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private Chip includeTitleCheckbox;
    private Chip includeHashtagsCheckbox;
    private Chip includeSourceCheckbox;
    private ExtendedFloatingActionButton generateFab;

    // Refinement UI
    private MaterialCardView refinementCard;
    private Chip checkRephrase;
    private Chip checkRecheckFlow;
    private Chip checkRecheckWording;
    private Chip checkFormal;
    private Chip checkConversational;
    private Chip checkShortenDetailed;
    private MaterialButton regenerateButton;
    private MaterialButton applyPillButton;

    // Pill selector
    private Chip pillSelectorChip;

    // Error state UI
    private MaterialCardView errorCard;
    private TextView errorMessage;
    private MaterialButton tryAgainButton;
    private boolean lastOperationWasRefinement = false;

    private PreferencesManager prefsManager;
    private NotificationHelper notificationHelper;
    private MainViewModel viewModel;

    private String originalGeneratedPost = "";
    private String generatedSourceCitation = "";
    private String generatedTitle = "";
    private String generatedBody = "";
    private String originalInputText = "";
    private boolean isEditMode = false;

    // Markwon for markdown rendering
    private Markwon markwon;
    private String rawOutputText = ""; // Store raw markdown text for editing

    /**
     * BroadcastReceiver for handling results from ContentGenerationService.
     */
    private final BroadcastReceiver serviceResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(ContentGenerationService.EXTRA_SUCCESS, false);
            boolean isRefinement = intent.getBooleanExtra(ContentGenerationService.EXTRA_IS_REFINEMENT, false);
            boolean isRateLimit = intent.getBooleanExtra(ContentGenerationService.EXTRA_IS_RATE_LIMIT, false);

            if (success) {
                String result = intent.getStringExtra(ContentGenerationService.EXTRA_RESULT);
                // Update ViewModel state - it will survive rotation
                handleGenerationSuccess(result, isRefinement);
            } else if (isRateLimit) {
                // Rate limit hit - show fallback dialog
                String provider = intent.getStringExtra(ContentGenerationService.EXTRA_RATE_LIMIT_PROVIDER);
                long retryDelayMs = intent.getLongExtra(ContentGenerationService.EXTRA_RETRY_DELAY_MS, 0);
                showRateLimitFallbackDialog(provider, retryDelayMs, isRefinement);
            } else {
                String error = intent.getStringExtra(ContentGenerationService.EXTRA_ERROR);
                // Update ViewModel state
                handleGenerationError(error, isRefinement);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "=== MainActivity onCreate ===");

        // Initialize preferences
        prefsManager = new PreferencesManager(this);
        notificationHelper = new NotificationHelper(this);

        // Initialize ViewModel (survives configuration changes)
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initialize Markwon for markdown rendering
        markwon = Markwon.create(this);

        // Apply saved theme before setContentView
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_main);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize views
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        editedIndicator = findViewById(R.id.editedIndicator);
        outputCard = findViewById(R.id.outputCard);
        skeletonCard = findViewById(R.id.skeletonCard);
        placeholderView = findViewById(R.id.placeholderView);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressText = findViewById(R.id.progressText);
        inputCharCount = findViewById(R.id.inputCharCount);
        outputWordCount = findViewById(R.id.outputWordCount);
        clearInputButton = findViewById(R.id.clearInputButton);
        resetAllButton = findViewById(R.id.resetAllButton);
        pasteButton = findViewById(R.id.pasteButton);
        editButton = findViewById(R.id.editButton);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        includeTitleCheckbox = findViewById(R.id.includeTitleCheckbox);
        includeHashtagsCheckbox = findViewById(R.id.includeHashtagsCheckbox);
        includeSourceCheckbox = findViewById(R.id.includeSourceCheckbox);
        generateFab = findViewById(R.id.generateFab);

        // Refinement UI
        refinementCard = findViewById(R.id.refinementCard);
        checkRephrase = findViewById(R.id.checkRephrase);
        checkRecheckFlow = findViewById(R.id.checkRecheckFlow);
        checkRecheckWording = findViewById(R.id.checkRecheckWording);
        checkFormal = findViewById(R.id.checkFormal);
        checkConversational = findViewById(R.id.checkConversational);
        checkShortenDetailed = findViewById(R.id.checkShortenDetailed);
        regenerateButton = findViewById(R.id.regenerateButton);
        applyPillButton = findViewById(R.id.applyPillButton);

        // Pill selector
        pillSelectorChip = findViewById(R.id.pillSelectorChip);
        pillSelectorChip.setOnClickListener(v -> showPillSelectorBottomSheet());

        // Error state UI
        errorCard = findViewById(R.id.errorCard);
        errorMessage = findViewById(R.id.errorMessage);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setOnClickListener(v -> onTryAgainClick());

        // Apply Pill button
        applyPillButton.setOnClickListener(v -> applyPillToRefinements());

        // Setup button listeners
        generateFab.setOnClickListener(v -> {
            // Add scale animation to FAB
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.9f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.9f, 1f);
            scaleX.setDuration(150);
            scaleY.setDuration(150);
            scaleX.start();
            scaleY.start();
            onGenerateClick();
        });
        editButton.setOnClickListener(v -> toggleEditMode());
        copyButton.setOnClickListener(v -> onCopyClick());
        shareButton.setOnClickListener(v -> onShareClick());
        clearInputButton.setOnClickListener(v -> onClearInputClick());
        resetAllButton.setOnClickListener(v -> onResetAllClick());
        pasteButton.setOnClickListener(v -> onPasteClick());
        regenerateButton.setOnClickListener(v -> onRegenerateClick());

        // Watch for text changes to update character count
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int charCount = s.length();
                inputCharCount.setText(charCount + " characters");
            }
        });

        // Watch for text changes to show edited indicator and update word count
        outputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditMode && !s.toString().equals(originalGeneratedPost)) {
                    editedIndicator.setVisibility(View.VISIBLE);
                } else {
                    editedIndicator.setVisibility(View.GONE);
                }

                // Update word count
                String text = s.toString().trim();
                int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
                outputWordCount.setText(wordCount + " words");
            }
        });

        // Check if API key is set
        if (!prefsManager.hasApiKey()) {
            Log.w(TAG, "API key not configured");
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_LONG).show();
        }

        // Handle incoming intent from ShareReceiverActivity
        handleIncomingIntent(getIntent());

        // Restore state from ViewModel after rotation (if any)
        restoreStateFromViewModel();
    }

    /**
     * Restores UI state from ViewModel after configuration change (e.g., rotation).
     * This is the key MVVM benefit - data survives screen rotation.
     */
    private void restoreStateFromViewModel() {
        GenerationState currentState = viewModel.getCurrentState();

        // Restore input text if available
        String savedInputText = viewModel.getCurrentInputText();
        if (savedInputText != null && !savedInputText.isEmpty()) {
            inputText.setText(savedInputText);
            originalInputText = viewModel.getOriginalInputText();
        }

        // Restore generated content if available
        if (currentState != null && currentState.hasContent()) {
            Log.i(TAG, "Restoring state from ViewModel after rotation");

            // Restore content from ViewModel
            generatedTitle = currentState.getGeneratedTitle();
            generatedBody = currentState.getGeneratedBody();
            generatedSourceCitation = currentState.getSourceCitation();
            originalGeneratedPost = viewModel.getOriginalGeneratedPost();

            // Rebuild UI
            rebuildOutputText();
            hidePlaceholder();
            showOutputCard();
            refinementCard.setVisibility(View.VISIBLE);
            clearRefinementCheckboxes();

            // Restore edit mode state
            isEditMode = viewModel.isEditMode();
            if (isEditMode) {
                outputText.setFocusable(true);
                outputText.setFocusableInTouchMode(true);
                editButton.setText(R.string.save_edit);
                editButton.setIconResource(android.R.drawable.ic_menu_save);

                // Restore user-edited content if any
                String userEdits = viewModel.getUserEditedContent();
                if (userEdits != null && !userEdits.isEmpty()) {
                    outputText.setText(userEdits);
                }
            }
        }
    }

    /**
     * Handles the incoming intent to continue work from ShareReceiverActivity.
     */
    private void handleIncomingIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        // Check if this is a "continue in main app" intent
        String sharedText = intent.getStringExtra("shared_text");
        String generatedContent = intent.getStringExtra("generated_content");
        String intentTitle = intent.getStringExtra("generated_title");
        String intentBody = intent.getStringExtra("generated_body");
        String intentSource = intent.getStringExtra("generated_source");

        if (sharedText != null && !sharedText.isEmpty()) {
            inputText.setText(sharedText);
            originalInputText = sharedText;
            Log.i(TAG, "Received shared text from ShareReceiverActivity");
        }

        if (generatedContent != null && !generatedContent.isEmpty()) {
            // Set the generated content parts
            if (intentTitle != null) {
                generatedTitle = intentTitle;
            }
            if (intentBody != null) {
                generatedBody = intentBody;
            }
            if (intentSource != null) {
                generatedSourceCitation = intentSource;
            }

            // Rebuild and display the output
            rebuildOutputText();

            // Show the output card and hide placeholder
            hidePlaceholder();
            showOutputCard();

            // Show refinement section
            refinementCard.setVisibility(View.VISIBLE);

            Log.i(TAG, "Loaded generated content from ShareReceiverActivity");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register broadcast receiver for service results
        LocalBroadcastManager.getInstance(this).registerReceiver(
                serviceResultReceiver,
                new IntentFilter(ContentGenerationService.BROADCAST_RESULT));

        // Reload preferences when returning to activity

        // Load hashtags enabled state
        includeHashtagsCheckbox.setChecked(prefsManager.areHashtagsEnabled());

        // Load source enabled state - Master Switch Logic
        boolean isSourceFeatureEnabled = prefsManager.isSourceEnabled();
        if (isSourceFeatureEnabled) {
            includeSourceCheckbox.setVisibility(View.VISIBLE);
            // Only set checked if it was previously unchecked (don't override user choice
            // if they unchecked it manually)
            // But for now, let's stick to the requirement: "When the source is toggled
            // on... included"
            // If we want to persist the user's checkbox choice across sessions, we'd need
            // another pref.
            // For now, let's default to TRUE if enabled, as per user implication.
            if (!includeSourceCheckbox.isChecked()) {
                includeSourceCheckbox.setChecked(true);
            }

            // Add listener for dynamic toggling
            includeSourceCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                toggleSourceCitation(isChecked);
            });
        } else {
            includeSourceCheckbox.setVisibility(View.GONE);
            includeSourceCheckbox.setChecked(false);
        }

        // Add listener for title toggling
        includeTitleCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleTitle(isChecked);
        });

        // Update hashtags checkbox visibility
        includeHashtagsCheckbox.setVisibility(
                !prefsManager.getHashtags().isEmpty() ? View.VISIBLE : View.GONE);

        // Update pill selector chip display
        updatePillChipDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save current state to ViewModel before pausing
        saveStateToViewModel();

        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceResultReceiver);
    }

    /**
     * Saves current UI state to ViewModel for preservation across rotation.
     */
    private void saveStateToViewModel() {
        // Save input text
        String currentInput = inputText.getText() != null ? inputText.getText().toString() : "";
        viewModel.setCurrentInputText(currentInput);
        viewModel.setOriginalInputText(originalInputText);

        // Save edit mode state
        viewModel.setEditMode(isEditMode);

        // Save user-edited content if in edit mode
        if (isEditMode && outputText.getText() != null) {
            viewModel.setUserEditedContent(outputText.getText().toString());
        }
    }

    /**
     * Toggles between edit and view mode.
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        Log.d(TAG, "Toggle edit mode: " + (isEditMode ? "EDIT" : "VIEW"));

        if (isEditMode) {
            // Enable editing - show raw markdown text
            outputText.setText(rawOutputText);
            outputText.setFocusable(true);
            outputText.setFocusableInTouchMode(true);
            outputText.requestFocus();
            editButton.setText(R.string.save_edit);
            editButton.setIconResource(android.R.drawable.ic_menu_save);
            // Disable scrolling in edit mode to allow text selection
            outputText.setMovementMethod(null);
        } else {
            // Disable editing - render markdown
            outputText.setFocusable(false);
            outputText.setFocusableInTouchMode(false);
            editButton.setText(R.string.edit_button);
            editButton.setIconResource(android.R.drawable.ic_menu_edit);

            // Save edited version
            if (outputText.getText() != null) {
                rawOutputText = outputText.getText().toString();
                originalGeneratedPost = rawOutputText;
                // Re-render markdown
                markwon.setMarkdown(outputText, rawOutputText);
            }
        }
    }

    /**
     * Toggles the source citation in the output text.
     */
    private void toggleSourceCitation(boolean show) {
        rebuildOutputText();
    }

    /**
     * Toggles the title in the output text.
     */
    private void toggleTitle(boolean show) {
        rebuildOutputText();
    }

    /**
     * Extracts the source citation from the generated text.
     * Returns the text WITHOUT the source citation.
     */
    private String extractSourceCitation(String fullResult) {
        if (fullResult == null) {
            generatedSourceCitation = "";
            return "";
        }

        // Regex to find the source citation line
        // Matches: "Sumber:", "*Sumber:*", "Source:", "Sumber :", etc.
        String regex = "(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(fullResult);

        if (matcher.find()) {
            generatedSourceCitation = matcher.group().trim();
            String contentWithoutSource = fullResult.replaceAll(regex, "").trim();
            // Clean up trailing newlines
            return contentWithoutSource.replaceAll("\\n+$", "").trim();
        } else {
            generatedSourceCitation = "";
            return fullResult;
        }
    }

    /**
     * Extracts the title (first line) and body from the content.
     * Stores them separately for dynamic toggling.
     */
    private void extractTitleAndBody(String content) {
        if (content == null || content.isEmpty()) {
            generatedTitle = "";
            generatedBody = "";
            return;
        }

        // Split by first double newline or single newline
        String[] parts = content.split("\\n\\n", 2);
        if (parts.length >= 2 && parts[0].length() < 150) {
            // First part is title (if reasonably short)
            generatedTitle = parts[0].trim();
            generatedBody = parts[1].trim();
        } else {
            // Try single newline
            parts = content.split("\\n", 2);
            if (parts.length >= 2 && parts[0].length() < 150) {
                generatedTitle = parts[0].trim();
                generatedBody = parts[1].trim();
            } else {
                // No clear title separation, treat all as body
                generatedTitle = "";
                generatedBody = content.trim();
            }
        }
    }

    /**
     * Rebuilds the output text based on current checkbox states.
     */
    private void rebuildOutputText() {
        StringBuilder textBuilder = new StringBuilder();

        // Add title if checked and available
        if (includeTitleCheckbox.isChecked() && !generatedTitle.isEmpty()) {
            textBuilder.append(generatedTitle).append("\n\n");
        }

        // Add body
        textBuilder.append(generatedBody);

        // Add source if checked and available
        if (includeSourceCheckbox.isChecked() && !generatedSourceCitation.isEmpty()) {
            textBuilder.append("\n\n").append(generatedSourceCitation);
        }

        String finalText = textBuilder.toString().trim();
        originalGeneratedPost = finalText;
        rawOutputText = finalText;

        // Render markdown if not in edit mode
        if (!isEditMode) {
            markwon.setMarkdown(outputText, finalText);
        } else {
            outputText.setText(finalText);
        }
    }

    /**
     * Handles the generate button click.
     */
    private void onGenerateClick() {
        Log.i(TAG, ">>> Generate button clicked <<<");
        String input = inputText.getText() != null ? inputText.getText().toString().trim() : "";
        Log.d(TAG, "Input length: " + input.length() + " characters");
        if (input.isEmpty()) {
            Log.w(TAG, "Generation aborted: empty input");
            Toast.makeText(this, R.string.input_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if API key is configured
        if (!prefsManager.hasApiKey()) {
            Log.w(TAG, "Generation aborted: API key not configured");
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Opening SettingsActivity");
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        // Hide placeholder, error card and show skeleton loading
        hidePlaceholder();
        hideErrorCard();
        showSkeletonLoading(true);

        // Store for potential regeneration
        originalInputText = input;

        // Start the Foreground Service for generation
        boolean includeSource = prefsManager.isSourceEnabled();
        Intent serviceIntent = new Intent(this, ContentGenerationService.class);
        serviceIntent.setAction(ContentGenerationService.ACTION_GENERATE);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INPUT_TEXT, input);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, includeSource);

        Log.i(TAG, "Starting ContentGenerationService for generation");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * Sets the output text with optional hashtags.
     */
    private void setOutputText(String text) {
        outputText.setText(text);
        outputText.setFocusable(false);
        outputText.setFocusableInTouchMode(false);
    }

    /**
     * Gets the final text to copy/share (with hashtags if enabled).
     */
    private String getFinalText() {
        // Use raw text for copy/share (not rendered markdown)
        String text = rawOutputText.isEmpty() ? (outputText.getText() != null ? outputText.getText().toString() : "")
                : rawOutputText;

        // Add hashtags if enabled
        if (includeHashtagsCheckbox.isChecked() && prefsManager.areHashtagsEnabled()) {
            String hashtags = prefsManager.getFormattedHashtags();
            if (!hashtags.isEmpty()) {
                text = text + "\n\n" + hashtags;
            }
        }

        return text;
    }

    /**
     * Handles the copy button click.
     */
    private void onCopyClick() {
        String textToCopy = getFinalText();

        if (textToCopy.isEmpty()) {
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Oreamnos Post", textToCopy);
        clipboard.setPrimaryClip(clip);
        Log.i(TAG, "Text copied to clipboard");
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the share button click.
     */
    private void onShareClick() {
        String textToShare = getFinalText();

        if (textToShare.isEmpty()) {
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        Log.i(TAG, "Opening share chooser");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_button)));
    }

    /**
     * Shows or hides the progress overlay.
     */
    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Clears the input text field.
     */
    private void onClearInputClick() {
        if (inputText.getText() != null && !inputText.getText().toString().isEmpty()) {
            inputText.setText("");
            Toast.makeText(this, R.string.input_cleared, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Resets the entire UI to initial state.
     * Clears input, output, refinement options, and shows placeholder.
     */
    private void onResetAllClick() {
        // Clear input
        if (inputText.getText() != null) {
            inputText.setText("");
        }

        // Hide output card
        outputCard.setVisibility(View.GONE);

        // Hide refinement card
        refinementCard.setVisibility(View.GONE);
        clearRefinementCheckboxes();

        // Show placeholder
        showPlaceholder();

        // Reset internal state
        originalGeneratedPost = "";
        generatedSourceCitation = "";
        generatedTitle = "";
        generatedBody = "";
        originalInputText = "";
        isEditMode = false;
        editButton.setText(R.string.edit_button);
        editButton.setIconResource(android.R.drawable.ic_menu_edit);
        editedIndicator.setVisibility(View.GONE);

        Toast.makeText(this, R.string.all_reset, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "UI reset to initial state");
    }

    /**
     * Pastes text from clipboard into the input field.
     */
    private void onPasteClick() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence pastedText = clip.getItemAt(0).getText();
                if (pastedText != null && pastedText.length() > 0) {
                    inputText.setText(pastedText);
                    inputText.setSelection(pastedText.length()); // Move cursor to end
                    Toast.makeText(this, R.string.text_pasted, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Text pasted from clipboard");
                    return;
                }
            }
        }
        Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows or hides the skeleton loading animation.
     * Also updates FAB state to show loading indicator.
     */
    private void showSkeletonLoading(boolean show) {
        if (show) {
            skeletonCard.setVisibility(View.VISIBLE);
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            skeletonCard.startAnimation(slideUp);

            // Update FAB to loading state
            generateFab.setText(R.string.generating_button);
            generateFab.setIcon(null); // Remove icon while loading
            generateFab.setEnabled(false);
        } else {
            skeletonCard.setVisibility(View.GONE);

            // Reset FAB to normal state
            generateFab.setText(R.string.generate_button);
            generateFab.setIconResource(android.R.drawable.ic_menu_send);
            generateFab.setEnabled(true);
        }
    }

    /**
     * Shows the placeholder illustration with fade in animation.
     */
    private void showPlaceholder() {
        if (placeholderView.getVisibility() != View.VISIBLE) {
            placeholderView.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            placeholderView.startAnimation(fadeIn);
        }
    }

    /**
     * Hides the placeholder illustration with fade out animation.
     */
    private void hidePlaceholder() {
        if (placeholderView.getVisibility() == View.VISIBLE) {
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    placeholderView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            placeholderView.startAnimation(fadeOut);
        }
    }

    /**
     * Shows the output card with slide up animation.
     */
    private void showOutputCard() {
        outputCard.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        outputCard.startAnimation(slideUp);
    }

    /**
     * Shows a dialog when rate limit is hit, offering to switch to an alternative
     * provider.
     *
     * @param currentProvider The provider that hit the rate limit
     * @param retryDelayMs    Suggested retry delay in milliseconds
     * @param isRefinement    Whether this was a refinement operation
     */
    private void showRateLimitFallbackDialog(String currentProvider, long retryDelayMs, boolean isRefinement) {
        // Hide skeleton loading
        showSkeletonLoading(false);

        // Determine fallback provider
        String fallbackProvider;
        String fallbackDisplayName;

        if (PreferencesManager.PROVIDER_GEMINI.equals(currentProvider)) {
            fallbackProvider = PreferencesManager.PROVIDER_GROQ;
            fallbackDisplayName = "Groq (Llama 3.3)";
        } else if (PreferencesManager.PROVIDER_GROQ.equals(currentProvider)) {
            fallbackProvider = PreferencesManager.PROVIDER_OPENROUTER;
            fallbackDisplayName = "OpenRouter";
        } else {
            fallbackProvider = PreferencesManager.PROVIDER_GEMINI;
            fallbackDisplayName = "Gemini";
        }

        // Check if fallback provider has API key
        boolean hasFallbackKey = false;
        switch (fallbackProvider) {
            case PreferencesManager.PROVIDER_GROQ:
                hasFallbackKey = prefsManager.getGroqApiKey() != null && !prefsManager.getGroqApiKey().isEmpty();
                break;
            case PreferencesManager.PROVIDER_OPENROUTER:
                hasFallbackKey = prefsManager.getOpenRouterApiKey() != null
                        && !prefsManager.getOpenRouterApiKey().isEmpty();
                break;
            case PreferencesManager.PROVIDER_GEMINI:
                hasFallbackKey = prefsManager.getApiKey() != null && !prefsManager.getApiKey().isEmpty();
                break;
        }

        String waitTimeMessage = "";
        if (retryDelayMs > 0) {
            int waitSeconds = (int) (retryDelayMs / 1000);
            if (waitSeconds > 60) {
                int minutes = waitSeconds / 60;
                waitTimeMessage = "Wait time: ~" + minutes + " minute" + (minutes > 1 ? "s" : "") + ".\n\n";
            } else {
                waitTimeMessage = "Wait time: ~" + waitSeconds + " second" + (waitSeconds > 1 ? "s" : "") + ".\n\n";
            }
        }

        String currentDisplayName = CuratorFactory.getProviderDisplayName(currentProvider);
        String message = currentDisplayName + " is rate limited (too many requests).\n\n" + waitTimeMessage;

        if (hasFallbackKey) {
            message += "Switch to " + fallbackDisplayName + " and retry immediately?";
        } else {
            message += "To use " + fallbackDisplayName + " as fallback, please configure its API key in Settings.";
        }

        final String finalFallbackProvider = fallbackProvider;
        final String finalFallbackDisplayName = fallbackDisplayName;
        final boolean finalHasFallbackKey = hasFallbackKey;
        final boolean finalIsRefinement = isRefinement;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rate_limit_title)
                .setMessage(message)
                .setPositiveButton(hasFallbackKey ? R.string.switch_and_retry : R.string.go_to_settings,
                        (dialog, which) -> {
                            if (finalHasFallbackKey) {
                                // Switch provider and retry
                                prefsManager.saveProvider(finalFallbackProvider);
                                Toast.makeText(this, "Switched to " + finalFallbackDisplayName, Toast.LENGTH_SHORT)
                                        .show();

                                // Retry the operation
                                if (finalIsRefinement) {
                                    onRegenerateClick();
                                } else {
                                    onGenerateClick();
                                }
                            } else {
                                // Go to settings
                                startActivity(new Intent(this, SettingsActivity.class));
                            }
                        })
                .setNegativeButton(R.string.wait_button, (dialog, which) -> {
                    dialog.dismiss();
                    // Show placeholder or error state
                    showPlaceholder();
                    Toast.makeText(this, "You can try again in a moment", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();

        Log.i(TAG, "Rate limit dialog shown. Current: " + currentProvider + ", Fallback: " + fallbackProvider);
    }

    /**
     * Handles the Regenerate button click.
     * Collects selected refinement options and regenerates the draft.
     */
    private void onRegenerateClick() {
        ArrayList<String> refinements = new ArrayList<>();

        // Collect selected refinement options
        if (checkRephrase.isChecked())
            refinements.add("rephrase");
        if (checkRecheckFlow.isChecked())
            refinements.add("recheck_flow");
        if (checkRecheckWording.isChecked())
            refinements.add("recheck_wording");
        if (checkFormal.isChecked())
            refinements.add("formal");
        if (checkConversational.isChecked())
            refinements.add("conversational");
        if (checkShortenDetailed.isChecked())
            refinements.add("shorten_detailed");

        // Ensure at least one option is selected
        if (refinements.isEmpty()) {
            Toast.makeText(this, "Please select at least one refinement option", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "Regenerating with refinements: " + refinements);

        // Show skeleton loading
        showSkeletonLoading(true);
        outputCard.setVisibility(View.GONE);
        refinementCard.setVisibility(View.GONE);

        // Start the Foreground Service for refinement
        boolean includeSource = prefsManager.isSourceEnabled();
        Intent serviceIntent = new Intent(this, ContentGenerationService.class);
        serviceIntent.setAction(ContentGenerationService.ACTION_REFINE);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_ORIGINAL_POST, originalGeneratedPost);
        serviceIntent.putStringArrayListExtra(ContentGenerationService.EXTRA_REFINEMENTS, refinements);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, includeSource);

        Log.i(TAG, "Starting ContentGenerationService for refinement");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * Handles successful generation/refinement result from the service.
     */
    private void handleGenerationSuccess(String result, boolean isRefinement) {
        Log.i(TAG, "Handling " + (isRefinement ? "refinement" : "generation") + " success");

        // Extract source citation first
        String contentWithoutSource = extractSourceCitation(result);

        // Extract title and body
        extractTitleAndBody(contentWithoutSource);

        // Save state to ViewModel (survives rotation)
        viewModel.setSuccess(generatedTitle, generatedBody, generatedSourceCitation, isRefinement);
        viewModel.setOriginalGeneratedPost(originalGeneratedPost);

        // Rebuild text based on checkbox states
        rebuildOutputText();

        showSkeletonLoading(false);
        showOutputCard();
        isEditMode = false;
        editButton.setText(R.string.edit_button);
        editButton.setIconResource(android.R.drawable.ic_menu_edit);
        editedIndicator.setVisibility(View.GONE);

        // Show refinement section
        refinementCard.setVisibility(View.VISIBLE);
        clearRefinementCheckboxes();
    }

    /**
     * Handles error result from the service.
     * Shows inline error state with retry button instead of toast.
     */
    private void handleGenerationError(String error, boolean isRefinement) {
        Log.e(TAG, "Handling " + (isRefinement ? "refinement" : "generation") + " error: " + error);

        showSkeletonLoading(false);
        lastOperationWasRefinement = isRefinement;

        // Show inline error card instead of toast
        showErrorCard(error, isRefinement);
    }

    /**
     * Shows the inline error card with message and retry button.
     */
    private void showErrorCard(String error, boolean isRefinement) {
        // Hide other views
        placeholderView.setVisibility(View.GONE);

        if (isRefinement) {
            // For refinement errors, keep the output visible
            outputCard.setVisibility(View.VISIBLE);
            refinementCard.setVisibility(View.VISIBLE);
        } else {
            outputCard.setVisibility(View.GONE);
            refinementCard.setVisibility(View.GONE);
        }

        // Set error message based on error type
        if (error != null && (error.toLowerCase().contains("network") ||
                error.toLowerCase().contains("connection") ||
                error.toLowerCase().contains("timeout"))) {
            errorMessage.setText(R.string.error_state_network);
        } else {
            errorMessage.setText(error != null ? error : getString(R.string.error_state_api));
        }

        // Show error card with animation
        errorCard.setVisibility(View.VISIBLE);
        errorCard.setAlpha(0f);
        errorCard.animate().alpha(1f).setDuration(300).start();
    }

    /**
     * Hides the error card.
     */
    private void hideErrorCard() {
        errorCard.setVisibility(View.GONE);
    }

    /**
     * Handler for Try Again button click.
     */
    private void onTryAgainClick() {
        hideErrorCard();

        if (lastOperationWasRefinement) {
            // Retry refinement
            onRegenerateClick();
        } else {
            // Retry generation
            onGenerateClick();
        }
    }

    /**
     * Clears all refinement checkboxes.
     */
    private void clearRefinementCheckboxes() {
        checkRephrase.setChecked(false);
        checkRecheckFlow.setChecked(false);
        checkRecheckWording.setChecked(false);
        checkFormal.setChecked(false);
        checkConversational.setChecked(false);
        checkShortenDetailed.setChecked(false);
    }

    /**
     * Applies the selected theme.
     */
    private void applyTheme(String theme) {
        int mode;
        switch (theme) {
            case PreferencesManager.THEME_LIGHT:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case PreferencesManager.THEME_DARK:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case PreferencesManager.THEME_SYSTEM:
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    // ==================== PILL SELECTOR ====================

    /**
     * Updates the pill selector chip to display the active pill.
     */
    private void updatePillChipDisplay() {
        GenerationPill activePill = prefsManager.getActivePill();
        if (activePill != null) {
            pillSelectorChip.setText(activePill.getName());
            // Use a tinted background to indicate active pill
            pillSelectorChip.setChipBackgroundColorResource(android.R.color.holo_green_dark);
            pillSelectorChip.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            // Show Apply Pill button in refinement section
            applyPillButton.setVisibility(View.VISIBLE);
            applyPillButton.setText(getString(R.string.apply_pill) + ": " + activePill.getName());
        } else {
            pillSelectorChip.setText(R.string.no_active_pill);
            pillSelectorChip.setChipBackgroundColorResource(android.R.color.transparent);
            pillSelectorChip.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            // Hide Apply Pill button when no active pill
            applyPillButton.setVisibility(View.GONE);
        }
    }

    /**
     * Applies the active pill's refinements to the refinement chips.
     */
    private void applyPillToRefinements() {
        GenerationPill activePill = prefsManager.getActivePill();
        if (activePill == null) {
            Toast.makeText(this, R.string.no_active_pill, Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear current selections
        clearRefinementCheckboxes();

        // Apply pill refinements
        List<String> refinements = activePill.getRefinements();
        for (String refinement : refinements) {
            switch (refinement) {
                case "rephrase":
                    checkRephrase.setChecked(true);
                    break;
                case "recheck_flow":
                    checkRecheckFlow.setChecked(true);
                    break;
                case "recheck_wording":
                    checkRecheckWording.setChecked(true);
                    break;
                case "shorten_detailed":
                    checkShortenDetailed.setChecked(true);
                    break;
            }
        }

        // Apply tone-based refinements
        String tone = activePill.getTone();
        if ("formal".equals(tone)) {
            checkFormal.setChecked(true);
        } else if ("casual".equals(tone)) {
            checkConversational.setChecked(true);
        }

        Toast.makeText(this, R.string.pill_applied, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows the bottom sheet for pill selection.
     */
    private void showPillSelectorBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pill_selector, null);
        dialog.setContentView(sheetView);

        ChipGroup pillChipGroup = sheetView.findViewById(R.id.pillChipGroup);
        TextView emptyText = sheetView.findViewById(R.id.emptyPillsText);

        List<GenerationPill> pills = prefsManager.getPills();
        String activePillId = prefsManager.getActivePillId();

        if (pills.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            pillChipGroup.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            pillChipGroup.setVisibility(View.VISIBLE);

            // Add "None" chip
            Chip noneChip = new Chip(this);
            noneChip.setText(R.string.no_active_pill);
            noneChip.setCheckable(true);
            noneChip.setChecked(activePillId == null);
            noneChip.setOnClickListener(v -> {
                prefsManager.saveActivePillId(null);
                Toast.makeText(this, R.string.pill_cleared, Toast.LENGTH_SHORT).show();
                updatePillChipDisplay();
                dialog.dismiss();
            });
            pillChipGroup.addView(noneChip);

            // Add pill chips
            for (GenerationPill pill : pills) {
                Chip chip = new Chip(this);
                chip.setText(pill.getName());
                chip.setCheckable(true);
                chip.setChecked(pill.getId().equals(activePillId));

                chip.setOnClickListener(v -> {
                    prefsManager.saveActivePillId(pill.getId());
                    Toast.makeText(this, R.string.pill_set_active, Toast.LENGTH_SHORT).show();
                    updatePillChipDisplay();
                    dialog.dismiss();
                });

                pillChipGroup.addView(chip);
            }
        }

        dialog.show();
    }

    /**
     * Sets up the bottom navigation with click listeners.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Set Generate as the default selected item
        bottomNav.setSelectedItemId(R.id.nav_generate);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_generate) {
                // Already on Generate screen - do nothing
                return true;
            } else if (itemId == R.id.nav_usage) {
                // Open Usage activity
                Intent intent = new Intent(this, UsageActivity.class);
                startActivity(intent);
                // Keep Generate selected since Usage is a separate activity
                bottomNav.setSelectedItemId(R.id.nav_generate);
                return false;
            } else if (itemId == R.id.nav_settings) {
                // Open Settings activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                // Keep Generate selected since Settings is a separate activity
                bottomNav.setSelectedItemId(R.id.nav_generate);
                return false;
            }

            return false;
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "=== MainActivity onDestroy ===");
        super.onDestroy();
    }
}
