package com.mycompany.oreamnos;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mycompany.oreamnos.services.ContentGenerationService;
import com.mycompany.oreamnos.utils.NotificationHelper;
import com.mycompany.oreamnos.utils.PreferencesManager;

import java.util.ArrayList;

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

    private PreferencesManager prefsManager;
    private NotificationHelper notificationHelper;

    private String originalGeneratedPost = "";
    private String generatedSourceCitation = "";
    private String generatedTitle = "";
    private String generatedBody = "";
    private String originalInputText = "";
    private boolean isEditMode = false;

    /**
     * BroadcastReceiver for handling results from ContentGenerationService.
     */
    private final BroadcastReceiver serviceResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(ContentGenerationService.EXTRA_SUCCESS, false);
            boolean isRefinement = intent.getBooleanExtra(ContentGenerationService.EXTRA_IS_REFINEMENT, false);

            if (success) {
                String result = intent.getStringExtra(ContentGenerationService.EXTRA_RESULT);
                handleGenerationSuccess(result, isRefinement);
            } else {
                String error = intent.getStringExtra(ContentGenerationService.EXTRA_ERROR);
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

        // Apply saved theme before setContentView
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_main);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceResultReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Toggles between edit and view mode.
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        Log.d(TAG, "Toggle edit mode: " + (isEditMode ? "EDIT" : "VIEW"));

        if (isEditMode) {
            // Enable editing
            outputText.setFocusable(true);
            outputText.setFocusableInTouchMode(true);
            outputText.requestFocus();
            editButton.setText(R.string.save_edit);
            editButton.setIconResource(android.R.drawable.ic_menu_save);
            // Disable scrolling in edit mode to allow text selection
            outputText.setMovementMethod(null);
        } else {
            // Disable editing
            outputText.setFocusable(false);
            outputText.setFocusableInTouchMode(false);
            editButton.setText(R.string.edit_button);
            editButton.setIconResource(android.R.drawable.ic_menu_edit);

            // Save edited version
            if (outputText.getText() != null) {
                originalGeneratedPost = outputText.getText().toString();
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
        outputText.setText(finalText);
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

        // Hide placeholder and show skeleton loading
        hidePlaceholder();
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
        String text = outputText.getText() != null ? outputText.getText().toString() : "";

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
     */
    private void handleGenerationError(String error, boolean isRefinement) {
        Log.e(TAG, "Handling " + (isRefinement ? "refinement" : "generation") + " error: " + error);

        showSkeletonLoading(false);

        if (isRefinement) {
            outputCard.setVisibility(View.VISIBLE);
            refinementCard.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Refinement error: " + error, Toast.LENGTH_LONG).show();
        } else {
            showPlaceholder();
            Toast.makeText(this, getString(R.string.processing_error, error), Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        Log.i(TAG, "=== MainActivity onDestroy ===");
        super.onDestroy();
    }
}
