package com.mycompany.oreamnos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.mycompany.oreamnos.services.GeminiService;
import com.mycompany.oreamnos.services.WebContentExtractor;
import com.mycompany.oreamnos.utils.NotificationHelper;
import com.mycompany.oreamnos.utils.PreferencesManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that handles shared content from other apps.
 * Receives text/URLs via share intent and processes them automatically.
 */
public class ShareReceiverActivity extends AppCompatActivity {

    private TextView sharedText;
    private TextView inputCharCount;
    private TextInputEditText outputText;
    private TextView outputWordCount;
    private TextView skeletonLoadingText;
    private TextView editedIndicator;
    private MaterialCardView resultCard;
    private MaterialCardView skeletonCard;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private MaterialButton editButton;
    private MaterialButton openMainAppButton;
    private Chip includeTitleCheckbox;
    private Chip includeHashtagsCheckbox;
    private Chip includeSourceCheckbox;

    private PreferencesManager prefsManager;
    private NotificationHelper notificationHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String lastGeneratedPost = "";
    private String generatedSourceCitation = "";
    private String generatedTitle = "";
    private String generatedBody = "";
    private String originalSharedContent = "";
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize preferences
        prefsManager = new PreferencesManager(this);
        notificationHelper = new NotificationHelper(this);

        // Apply saved theme before setContentView
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_share_receiver);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        sharedText = findViewById(R.id.sharedText);
        inputCharCount = findViewById(R.id.inputCharCount);
        outputText = findViewById(R.id.outputText);
        outputWordCount = findViewById(R.id.outputWordCount);
        resultCard = findViewById(R.id.resultCard);
        skeletonCard = findViewById(R.id.skeletonCard);
        skeletonLoadingText = findViewById(R.id.skeletonLoadingText);
        editedIndicator = findViewById(R.id.editedIndicator);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        editButton = findViewById(R.id.editButton);
        openMainAppButton = findViewById(R.id.openMainAppButton);
        includeTitleCheckbox = findViewById(R.id.includeTitleCheckbox);
        includeHashtagsCheckbox = findViewById(R.id.includeHashtagsCheckbox);
        includeSourceCheckbox = findViewById(R.id.includeSourceCheckbox);

        // Setup button listeners
        copyButton.setOnClickListener(v -> onCopyClick());
        shareButton.setOnClickListener(v -> onShareClick());
        editButton.setOnClickListener(v -> toggleEditMode());
        openMainAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("shared_text", originalSharedContent);
            intent.putExtra("generated_content", lastGeneratedPost);
            intent.putExtra("generated_title", generatedTitle);
            intent.putExtra("generated_body", generatedBody);
            intent.putExtra("generated_source", generatedSourceCitation);
            startActivity(intent);
            finish();
        });

        // Setup chip listeners
        includeTitleCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> rebuildOutputText());
        includeHashtagsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> rebuildOutputText());
        includeSourceCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> rebuildOutputText());

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
                if (isEditMode && !s.toString().equals(lastGeneratedPost)) {
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

        // Process the shared content
        processSharedContent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load hashtags enabled state
        includeHashtagsCheckbox.setChecked(prefsManager.areHashtagsEnabled());

        // Load source enabled state - Master Switch Logic
        boolean isSourceFeatureEnabled = prefsManager.isSourceEnabled();
        if (isSourceFeatureEnabled) {
            includeSourceCheckbox.setVisibility(View.VISIBLE);
            if (!includeSourceCheckbox.isChecked()) {
                includeSourceCheckbox.setChecked(true);
            }
        } else {
            includeSourceCheckbox.setVisibility(View.GONE);
            includeSourceCheckbox.setChecked(false);
        }

        // Update hashtags checkbox visibility
        includeHashtagsCheckbox.setVisibility(
                !prefsManager.getHashtags().isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Processes the shared content from the intent.
     */
    private void processSharedContent() {
        String content = getSharedContent();

        if (content == null || content.isEmpty()) {
            Toast.makeText(this, "No content received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Store original content
        originalSharedContent = content;

        // Display preview
        sharedText.setText(content);
        inputCharCount.setText(content.length() + " characters");

        // Check if API key is configured
        if (!prefsManager.hasApiKey()) {
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_LONG).show();
            return;
        }

        // Automatically start processing
        processContent(content);
    }

    /**
     * Gets the shared content from the intent.
     */
    private String getSharedContent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                return intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        return null;
    }

    /**
     * Processes the content using Gemini API.
     */
    private void processContent(String content) {
        showSkeleton(true);
        skeletonLoadingText.setText(R.string.processing);

        // Show progress notification
        notificationHelper.showProgressNotification(
                getString(R.string.notification_generating_title),
                getString(R.string.notification_generating_message));

        executor.execute(() -> {
            try {
                String textToProcess = content;

                // Check if content is a URL
                if (WebContentExtractor.isUrl(content)) {
                    mainHandler.post(() -> skeletonLoadingText.setText(R.string.extracting_content));
                    WebContentExtractor extractor = new WebContentExtractor();
                    textToProcess = extractor.extractContent(content);
                }

                // Generate post with Gemini
                mainHandler.post(() -> skeletonLoadingText.setText(R.string.generating_post));
                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                String tone = prefsManager.getTone();

                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);
                boolean includeSource = prefsManager.isSourceEnabled();
                String result = gemini.curatePost(textToProcess, includeSource);

                // Update UI on main thread
                String finalResult = result;
                mainHandler.post(() -> {
                    // Extract source citation first
                    String contentWithoutSource = extractSourceCitation(finalResult);

                    // Extract title and body
                    extractTitleAndBody(contentWithoutSource);

                    // Rebuild text based on checkbox states
                    rebuildOutputText();

                    resultCard.setVisibility(View.VISIBLE);
                    showSkeleton(false);

                    // Show completion notification
                    notificationHelper.showCompletedNotification(
                            getString(R.string.notification_complete_title),
                            getString(R.string.notification_complete_message));
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showSkeleton(false);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(ShareReceiverActivity.this,
                            getString(R.string.processing_error, errorMsg),
                            Toast.LENGTH_LONG).show();

                    // Show error notification
                    notificationHelper.showErrorNotification(
                            getString(R.string.notification_error_title),
                            errorMsg);
                });
            }
        });
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

        // Add hashtags if checked
        if (includeHashtagsCheckbox.isChecked() && prefsManager.areHashtagsEnabled()) {
            String hashtags = prefsManager.getFormattedHashtags();
            if (!hashtags.isEmpty()) {
                textBuilder.append("\n\n").append(hashtags);
            }
        }

        // Add source if checked and available
        if (includeSourceCheckbox.isChecked() && !generatedSourceCitation.isEmpty()) {
            textBuilder.append("\n\n").append(generatedSourceCitation);
        }

        String finalText = textBuilder.toString().trim();
        lastGeneratedPost = finalText;
        outputText.setText(finalText);
    }

    /**
     * Toggles between edit and view mode.
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            // Enable editing
            outputText.setFocusable(true);
            outputText.setFocusableInTouchMode(true);
            outputText.requestFocus();
            editButton.setText(R.string.save_edit);
            editButton.setIconResource(android.R.drawable.ic_menu_save);
        } else {
            // Disable editing
            outputText.setFocusable(false);
            outputText.setFocusableInTouchMode(false);
            editButton.setText(R.string.edit_button);
            editButton.setIconResource(android.R.drawable.ic_menu_edit);

            // Save edited version
            if (outputText.getText() != null) {
                lastGeneratedPost = outputText.getText().toString();
            }
        }
    }

    /**
     * Gets the final text to copy/share.
     */
    private String getFinalText() {
        return outputText.getText() != null ? outputText.getText().toString() : "";
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
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_button)));
    }

    /**
     * Shows or hides the skeleton loading.
     */
    private void showSkeleton(boolean show) {
        skeletonCard.setVisibility(show ? View.VISIBLE : View.GONE);
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
        super.onDestroy();
        executor.shutdown();
    }
}
