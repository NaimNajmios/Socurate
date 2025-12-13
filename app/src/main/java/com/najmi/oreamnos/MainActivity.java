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
import android.view.animation.OvershootInterpolator;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.najmi.oreamnos.curator.CuratorFactory;
import com.najmi.oreamnos.model.GenerationPill;
import com.najmi.oreamnos.services.ContentGenerationService;
import com.najmi.oreamnos.utils.NotificationHelper;
import com.najmi.oreamnos.utils.PreferencesManager;
import com.najmi.oreamnos.utils.ReadabilityUtils;
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
    private TextView readabilityScore;
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
    private Chip includeEmojisCheckbox;
    private MaterialSwitch keepStructureSwitch;
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

    // Custom refinement pills
    private ChipGroup customPillChips;
    private Chip addCustomPillChip;

    // Error state UI
    private MaterialCardView errorCard;
    private TextView errorMessage;
    private MaterialButton tryAgainButton;
    private boolean lastOperationWasRefinement = false;

    // URL Preview
    private MaterialCardView urlPreviewCard;
    private ImageView previewFavicon;
    private TextView previewTitle;
    private TextView previewDomain;
    private ImageButton closePreviewButton;
    private String detectedUrl = "";
    private Handler urlCheckHandler = new Handler(Looper.getMainLooper());
    private Runnable urlCheckRunnable;

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
    private String lastClipboardUrl = ""; // Track last clipboard URL to avoid repeat prompts

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
        readabilityScore = findViewById(R.id.readabilityScore);
        clearInputButton = findViewById(R.id.clearInputButton);
        resetAllButton = findViewById(R.id.resetAllButton);
        pasteButton = findViewById(R.id.pasteButton);
        editButton = findViewById(R.id.editButton);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        includeTitleCheckbox = findViewById(R.id.includeTitleCheckbox);
        includeHashtagsCheckbox = findViewById(R.id.includeHashtagsCheckbox);
        includeSourceCheckbox = findViewById(R.id.includeSourceCheckbox);
        includeEmojisCheckbox = findViewById(R.id.includeEmojisCheckbox);
        keepStructureSwitch = findViewById(R.id.keepStructureSwitch);
        generateFab = findViewById(R.id.generateFab);

        // URL Preview
        urlPreviewCard = findViewById(R.id.urlPreviewCard);
        previewFavicon = findViewById(R.id.previewFavicon);
        previewTitle = findViewById(R.id.previewTitle);
        previewDomain = findViewById(R.id.previewDomain);
        closePreviewButton = findViewById(R.id.closePreviewButton);

        // Refinement UI
        refinementCard = findViewById(R.id.refinementCard);
        checkRephrase = findViewById(R.id.checkRephrase);
        checkRecheckFlow = findViewById(R.id.checkRecheckFlow);
        checkRecheckWording = findViewById(R.id.checkRecheckWording);
        checkFormal = findViewById(R.id.checkFormal);
        checkConversational = findViewById(R.id.checkConversational);
        checkShortenDetailed = findViewById(R.id.checkShortenDetailed);
        regenerateButton = findViewById(R.id.regenerateButton);

        // Custom refinement pills
        customPillChips = findViewById(R.id.customPillChips);
        addCustomPillChip = findViewById(R.id.addCustomPillChip);
        addCustomPillChip.setOnClickListener(v -> showCreateCustomPillDialog(null));

        // Error state UI
        errorCard = findViewById(R.id.errorCard);
        errorMessage = findViewById(R.id.errorMessage);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setOnClickListener(v -> onTryAgainClick());

        // Setup button listeners
        generateFab.setOnClickListener(v -> {
            // Apply spring animation to FAB
            applyFabSpringAnimation(v);
            onGenerateClick();
        });
        editButton.setOnClickListener(v -> toggleEditMode());
        copyButton.setOnClickListener(v -> onCopyClick());
        shareButton.setOnClickListener(v -> onShareClick());
        clearInputButton.setOnClickListener(v -> onClearInputClick());
        resetAllButton.setOnClickListener(v -> onResetAllClick());
        pasteButton.setOnClickListener(v -> onPasteClick());
        regenerateButton.setOnClickListener(v -> onRegenerateClick());

        // Close preview button
        closePreviewButton.setOnClickListener(v -> {
            urlPreviewCard.setVisibility(View.GONE);
            detectedUrl = "";
        });

        // Watch for text changes to update character count
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce URL check
                if (urlCheckRunnable != null) {
                    urlCheckHandler.removeCallbacks(urlCheckRunnable);
                }
                urlCheckRunnable = () -> checkAndPreviewUrl(s.toString());
                urlCheckHandler.postDelayed(urlCheckRunnable, 500);
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

                // Update readability score
                double score = ReadabilityUtils.calculateFleschKincaidGradeLevel(text);
                readabilityScore.setText(String.format("Grade: %.1f", score));
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

        // Add listener for emojis toggling
        includeEmojisCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rebuildOutputText();
        });

        // Update hashtags checkbox visibility
        includeHashtagsCheckbox.setVisibility(
                !prefsManager.getHashtags().isEmpty() ? View.VISIBLE : View.GONE);

        // Load custom refinement pills
        loadCustomPillChips();

        // Check clipboard for football URLs
        checkClipboardForFootballUrl();
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
            String titleText = generatedTitle;
            if (!includeEmojisCheckbox.isChecked()) {
                titleText = stripLeadingEmojis(titleText);
            }
            textBuilder.append(titleText).append("\n\n");
        }

        // Add body
        String bodyText = generatedBody;
        if (!includeEmojisCheckbox.isChecked()) {
            // If emojis are disabled, strip them all
            bodyText = stripLeadingEmojis(bodyText);
        } else if (includeTitleCheckbox.isChecked() && !generatedTitle.isEmpty()) {
            // If emojis are enabled BUT title is shown (and has emoji),
            // strip emoji from the body to avoid double emojis.
            // The prompt puts emojis on BOTH Title and First Para, so we remove the Body
            // one here.
            bodyText = stripLeadingEmojis(bodyText);
        }
        // If emojis are enabled AND title is NOT shown, we keep the Body emoji (from
        // the prompt).

        textBuilder.append(bodyText);

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
        boolean keepStructure = keepStructureSwitch.isChecked();
        Intent serviceIntent = new Intent(this, ContentGenerationService.class);
        serviceIntent.setAction(ContentGenerationService.ACTION_GENERATE);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INPUT_TEXT, input);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, includeSource);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_KEEP_STRUCTURE, keepStructure);

        Log.i(TAG, "Starting ContentGenerationService for generation");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * Checks if the input text contains a URL and shows a preview if found.
     */
    private void checkAndPreviewUrl(String text) {
        if (text == null || text.trim().isEmpty()) {
            runOnUiThread(() -> {
                if (urlPreviewCard != null)
                    urlPreviewCard.setVisibility(View.GONE);
            });
            return;
        }

        // Simple check if text IS a URL (not just contains one)
        // We only want to trigger this if the user pasted a link directly
        if (com.najmi.oreamnos.services.WebContentExtractor.isUrl(text)) {
            String url = text.trim();
            if (!url.equals(detectedUrl)) {
                detectedUrl = url;

                // Optimistic UI: Show domain immediately
                try {
                    java.net.URL netUrl = new java.net.URL(url);
                    String domain = netUrl.getHost();
                    if (domain.startsWith("www.")) {
                        domain = domain.substring(4);
                    }
                    previewTitle.setText("Loading...");
                    previewDomain.setText(domain);
                    urlPreviewCard.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    // If URL parsing fails, just wait for full fetch
                }

                fetchUrlMetadata(url);
            }
        } else {
            runOnUiThread(() -> {
                if (urlPreviewCard != null)
                    urlPreviewCard.setVisibility(View.GONE);
            });
            detectedUrl = "";
        }
    }

    /**
     * Fetches metadata for the URL and updates the preview card.
     */
    private void fetchUrlMetadata(String url) {
        // Show loading state in preview card? Or just wait?
        // For now, let's just fetch silently and show when ready

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                com.najmi.oreamnos.services.WebContentExtractor extractor = new com.najmi.oreamnos.services.WebContentExtractor();
                com.najmi.oreamnos.services.WebContentExtractor.UrlMetadata metadata = extractor.extractMetadata(url);

                new Handler(Looper.getMainLooper()).post(() -> showUrlPreview(metadata));
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch metadata: " + e.getMessage());
                // Don't show error, just don't show preview
            } finally {
                executor.shutdown();
            }
        });
    }

    /**
     * Updates the UI with the URL metadata.
     */
    private void showUrlPreview(com.najmi.oreamnos.services.WebContentExtractor.UrlMetadata metadata) {
        if (isFinishing() || isDestroyed())
            return;

        previewTitle.setText(metadata.title != null ? metadata.title : "No Title");
        previewDomain.setText(metadata.domain);

        // Load favicon (placeholder for now, would need an image loading library like
        // Glide/Picasso)
        // Since we don't have Glide, we'll try to load it manually or just show the
        // domain icon
        // For this implementation, we'll stick to the default icon but if we had Glide:
        // Glide.with(this).load(metadata.faviconUrl).into(previewFavicon);

        urlPreviewCard.setVisibility(View.VISIBLE);

        // Show Snackbar to prompt generation
        com.google.android.material.snackbar.Snackbar
                .make(generateFab, "Link detected. Generate post?",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction("Generate", v -> {
                    // Apply spring animation to FAB
                    applyFabSpringAnimation(generateFab);
                    onGenerateClick();
                })
                .setAnchorView(generateFab)
                .show();
    }

    private void setOutputText(String text) {
        outputText.setText(text);
        outputText.setFocusable(false);
        outputText.setFocusableInTouchMode(false);
    }

    /**
     * Gets the final text to copy/share (with hashtags if enabled).
     * Strips markdown formatting for clean paste.
     */
    private String getFinalText() {
        // Use raw text for copy/share (not rendered markdown)
        String text = rawOutputText.isEmpty() ? (outputText.getText() != null ? outputText.getText().toString() : "")
                : rawOutputText;

        // Strip markdown formatting for clean copy/paste
        text = stripMarkdownFormatting(text);

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
     * Strips markdown formatting from text for clean clipboard copy.
     * Removes bold, italic, headers, links, etc. while preserving the actual text
     * content.
     */
    private String stripMarkdownFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Remove bold: **text** or __text__
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("__(.+?)__", "$1");

        // Remove italic: *text* or _text_ (simple approach)
        text = text.replaceAll("(?<!\\*)\\*(?!\\*)([^*]+)(?<!\\*)\\*(?!\\*)", "$1");
        text = text.replaceAll("(?<!_)_(?!_)([^_]+)(?<!_)_(?!_)", "$1");

        // Remove strikethrough: ~~text~~
        text = text.replaceAll("~~(.+?)~~", "$1");

        // Remove headers: # Header -> Header
        text = text.replaceAll("(?m)^#{1,6}\\s*", "");

        // Remove inline code: `code`
        text = text.replaceAll("`([^`]+)`", "$1");

        // Remove links: [text](url) -> text
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");

        // Remove images: ![alt](url) -> alt
        text = text.replaceAll("!\\[([^\\]]*?)\\]\\([^)]+\\)", "$1");

        // Remove blockquotes: > text -> text
        text = text.replaceAll("(?m)^>\\s*", "");

        // Remove horizontal rules
        text = text.replaceAll("(?m)^[-*_]{3,}$", "");

        // Clean up extra whitespace but preserve paragraph breaks
        text = text.replaceAll("\n{3,}", "\n\n");

        return text.trim();
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
     * Shows the output card with slide up animation and glow effect.
     */
    private void showOutputCard() {
        outputCard.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        outputCard.startAnimation(slideUp);

        // Apply temporary glow effect for fresh content
        showOutputCardGlow();
    }

    /**
     * Applies a spring bounce animation to the FAB using OvershootInterpolator.
     */
    private void applyFabSpringAnimation(View fab) {
        // Press animation
        Animation pressAnim = AnimationUtils.loadAnimation(this, R.anim.fab_spring_press);
        pressAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Release animation with bounce
                Animation releaseAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_spring_release);
                fab.startAnimation(releaseAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        fab.startAnimation(pressAnim);
    }

    /**
     * Shows a subtle glow effect on the output card for fresh content.
     * The glow fades out after 3 seconds.
     */
    private void showOutputCardGlow() {
        // Apply glow background
        outputCard.setStrokeColor(getResources().getColor(R.color.glow_accent, getTheme()));
        outputCard.setStrokeWidth(3);

        // Fade out glow after 3 seconds
        new Handler().postDelayed(() -> {
            // Animate back to normal stroke
            ObjectAnimator strokeAnim = ObjectAnimator.ofArgb(
                    outputCard,
                    "strokeColor",
                    getResources().getColor(R.color.glow_accent, getTheme()),
                    getResources().getColor(android.R.color.transparent, getTheme()));
            strokeAnim.setDuration(500);
            strokeAnim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Reset to normal outline
                    outputCard.setStrokeColor(getResources().getColor(R.color.md_theme_light_outline, getTheme()));
                    outputCard.setStrokeWidth(1);
                }
            });
            strokeAnim.start();
        }, 3000);
    }

    /**
     * Animates refinement chips with Material Motion stagger effect.
     */
    private void animateRefinementChipsWithStagger() {
        ChipGroup builtInChips = findViewById(R.id.builtInRefinementChips);
        if (builtInChips == null)
            return;

        int staggerDelay = 50; // 50ms delay between each chip

        for (int i = 0; i < builtInChips.getChildCount(); i++) {
            View chip = builtInChips.getChildAt(i);
            chip.setAlpha(0f);
            chip.setScaleX(0.8f);
            chip.setScaleY(0.8f);

            final int delay = i * staggerDelay;
            new Handler().postDelayed(() -> {
                Animation chipAnim = AnimationUtils.loadAnimation(this, R.anim.chip_stagger_item);
                chip.startAnimation(chipAnim);
                chip.setAlpha(1f);
                chip.setScaleX(1f);
                chip.setScaleY(1f);
            }, delay);
        }

        // Also animate custom pill chips
        if (customPillChips != null) {
            int baseDelay = builtInChips.getChildCount() * staggerDelay;
            for (int i = 0; i < customPillChips.getChildCount(); i++) {
                View chip = customPillChips.getChildAt(i);
                chip.setAlpha(0f);
                chip.setScaleX(0.8f);
                chip.setScaleY(0.8f);

                final int delay = baseDelay + (i * staggerDelay);
                new Handler().postDelayed(() -> {
                    Animation chipAnim = AnimationUtils.loadAnimation(this, R.anim.chip_stagger_item);
                    chip.startAnimation(chipAnim);
                    chip.setAlpha(1f);
                    chip.setScaleX(1f);
                    chip.setScaleY(1f);
                }, delay);
            }
        }
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

        // Show refinement section with stagger animation
        refinementCard.setVisibility(View.VISIBLE);
        clearRefinementCheckboxes();
        animateRefinementChipsWithStagger();
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

    // ==================== CLIPBOARD URL DETECTION ====================

    /**
     * Checks clipboard for football-related URLs and shows a dialog to generate.
     */
    private void checkClipboardForFootballUrl() {
        // Don't show if input already has content
        String currentInput = inputText.getText() != null ? inputText.getText().toString().trim() : "";
        if (!currentInput.isEmpty()) {
            return;
        }

        // Get clipboard content
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            return;
        }

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return;
        }

        CharSequence clipText = clip.getItemAt(0).getText();
        if (clipText == null) {
            return;
        }

        String url = clipText.toString().trim();

        // Check if it's a URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return;
        }

        // Don't show again for the same URL
        if (url.equals(lastClipboardUrl)) {
            return;
        }

        // Check if it's a football-related URL
        if (!isFootballUrl(url)) {
            return;
        }

        // Update last clipboard URL
        lastClipboardUrl = url;

        // Show dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clipboard_url_detected)
                .setMessage(getString(R.string.clipboard_url_message) + "\n\n" + truncateUrl(url))
                .setPositiveButton(R.string.clipboard_generate, (dialog, which) -> {
                    inputText.setText(url);
                    onGenerateClick();
                })
                .setNegativeButton(R.string.clipboard_dismiss, null)
                .show();
    }

    /**
     * Checks if a URL is likely a football-related site.
     */
    private boolean isFootballUrl(String url) {
        if (url == null)
            return false;
        String lower = url.toLowerCase();

        // Football news sites
        String[] footballDomains = {
                "skysports.com", "bbc.com/sport", "bbc.co.uk/sport",
                "theathletic.com", "goal.com", "espn.com/soccer", "espnfc.com",
                "90min.com", "football365.com", "fourfourtwo.com",
                "transfermarkt.com", "whoscored.com", "sofascore.com",
                "theguardian.com/football", "mirror.co.uk/sport/football",
                "telegraph.co.uk/football", "dailymail.co.uk/sport/football",
                "independent.co.uk/sport/football", "sportingnews.com/soccer",
                "footballtransfers.com", "fabrizio romano", "football.london",
                "manutd.com", "liverpoolfc.com", "mancity.com", "arsenal.com",
                "chelseafc.com", "tottenhamhotspur.com", "fcbarcelona.com",
                "realmadrid.com", "juventus.com", "psg.fr", "bayernmunich.com"
        };

        for (String domain : footballDomains) {
            if (lower.contains(domain)) {
                return true;
            }
        }

        // Also check for common football keywords in URL path
        String[] footballKeywords = {
                "/football/", "/soccer/", "/premier-league/", "/la-liga/",
                "/bundesliga/", "/serie-a/", "/champions-league/", "/transfers/"
        };

        for (String keyword : footballKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Truncates URL for display in dialog.
     */
    private String truncateUrl(String url) {
        if (url == null)
            return "";
        if (url.length() <= 60)
            return url;
        return url.substring(0, 57) + "...";
    }

    /**
     * Strips leading emojis from paragraphs.
     */
    private String stripLeadingEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        // Regex to match emojis at the start of lines/paragraphs
        // This regex matches common emoji ranges and whitespace
        String emojiRegex = "^[\\uD83C\\uDF00-\\uD83D\\uDDFF\\uD83E\\uDD00-\\uD83E\\uDDFF\\uD83D\\uDE00-\\uD83D\\uDE4F\\uD83D\\uDE80-\\uD83D\\uDEFF\\u2600-\\u26FF\\u2700-\\u27BF]+\\s*";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Replace leading emoji and whitespace
            line = line.replaceAll(emojiRegex, "");
            sb.append(line);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    // ==================== CUSTOM REFINEMENT PILLS ====================

    /**
     * Shows dialog to create or edit a custom refinement pill.
     */
    private void showCreateCustomPillDialog(com.najmi.oreamnos.model.GenerationPill existingPill) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_pill, null);

        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        com.google.android.material.textfield.TextInputEditText nameInput = dialogView.findViewById(R.id.pillNameInput);
        com.google.android.material.textfield.TextInputEditText commandInput = dialogView
                .findViewById(R.id.pillCommandInput);
        com.google.android.material.button.MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        com.google.android.material.button.MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        // Set title based on create or edit mode
        boolean isEdit = existingPill != null;
        titleView.setText(isEdit ? R.string.edit_custom_pill : R.string.create_custom_pill);

        // Pre-fill if editing
        if (isEdit) {
            nameInput.setText(existingPill.getName());
            commandInput.setText(existingPill.getCommand());
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String command = commandInput.getText() != null ? commandInput.getText().toString().trim() : "";

            if (name.isEmpty() || command.isEmpty()) {
                Toast.makeText(this, "Please enter both name and command", Toast.LENGTH_SHORT).show();
                return;
            }

            com.najmi.oreamnos.model.GenerationPill pill;
            if (isEdit) {
                pill = existingPill;
                pill.setName(name);
                pill.setCommand(command);
            } else {
                pill = new com.najmi.oreamnos.model.GenerationPill(name, command);
            }

            prefsManager.savePill(pill);
            Toast.makeText(this, R.string.pill_saved, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadCustomPillChips();
        });

        dialog.show();
    }

    /**
     * Loads custom pill chips from preferences and adds them to the ChipGroup.
     */
    private void loadCustomPillChips() {
        // Remove all chips except the add button
        int childCount = customPillChips.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = customPillChips.getChildAt(i);
            if (child.getId() != R.id.addCustomPillChip) {
                customPillChips.removeView(child);
            }
        }

        // Load pills and add chips
        java.util.List<com.najmi.oreamnos.model.GenerationPill> pills = prefsManager.getPills();
        for (com.najmi.oreamnos.model.GenerationPill pill : pills) {
            Chip chip = new Chip(this);
            chip.setText(pill.getName());
            chip.setTag(pill);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, getTheme()));
            chip.setChipStrokeWidth(1 * getResources().getDisplayMetrics().density); // 1dp
            chip.setCheckedIconVisible(true);

            // Long press to edit/delete
            chip.setOnLongClickListener(v -> {
                showPillOptionsMenu(pill, chip);
                return true;
            });

            // Insert before add button
            int addButtonIndex = customPillChips.indexOfChild(addCustomPillChip);
            customPillChips.addView(chip, addButtonIndex);
        }
    }

    /**
     * Shows options menu (edit/delete) for a custom pill.
     */
    private void showPillOptionsMenu(com.najmi.oreamnos.model.GenerationPill pill, View anchor) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, R.string.edit_custom_pill);
        popup.getMenu().add(0, 2, 0, R.string.pill_deleted);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showCreateCustomPillDialog(pill);
                return true;
            } else if (item.getItemId() == 2) {
                confirmDeletePill(pill);
                return true;
            }
            return false;
        });

        popup.show();
    }

    /**
     * Shows confirmation dialog to delete a custom pill.
     */
    private void confirmDeletePill(com.najmi.oreamnos.model.GenerationPill pill) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.pill_deleted)
                .setMessage(R.string.delete_pill_confirm)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    prefsManager.deletePill(pill.getId());
                    Toast.makeText(this, R.string.pill_deleted, Toast.LENGTH_SHORT).show();
                    loadCustomPillChips();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Gets commands from all selected custom pill chips.
     */
    private java.util.List<String> getSelectedCustomPillCommands() {
        java.util.List<String> commands = new java.util.ArrayList<>();
        int childCount = customPillChips.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = customPillChips.getChildAt(i);
            if (child instanceof Chip && child.getId() != R.id.addCustomPillChip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    com.najmi.oreamnos.model.GenerationPill pill = (com.najmi.oreamnos.model.GenerationPill) chip
                            .getTag();
                    if (pill != null && pill.getCommand() != null && !pill.getCommand().isEmpty()) {
                        commands.add(pill.getCommand());
                    }
                }
            }
        }
        return commands;
    }
}
