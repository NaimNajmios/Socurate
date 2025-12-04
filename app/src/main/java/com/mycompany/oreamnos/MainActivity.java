package com.mycompany.oreamnos;

import android.animation.ObjectAnimator;
import android.util.Log;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mycompany.oreamnos.services.GeminiService;
import com.mycompany.oreamnos.services.WebContentExtractor;
import com.mycompany.oreamnos.utils.NotificationHelper;
import com.mycompany.oreamnos.utils.PreferencesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MaterialCardView outputCard;
    private MaterialCardView skeletonCard;
    private View progressOverlay;
    private View placeholderView;
    private ImageButton clearInputButton;
    private MaterialButton editButton;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private MaterialCheckBox includeHashtagsCheckbox;
    private MaterialCheckBox includeSourceCheckbox;
    private ExtendedFloatingActionButton generateFab;

    // Refinement UI
    private MaterialCardView refinementCard;
    private MaterialCheckBox checkRephrase;
    private MaterialCheckBox checkRecheckFlow;
    private MaterialCheckBox checkRecheckWording;
    private MaterialCheckBox checkFormal;
    private MaterialCheckBox checkConversational;
    private MaterialCheckBox checkShortenDetailed;
    private MaterialButton regenerateButton;

    private PreferencesManager prefsManager;
    private NotificationHelper notificationHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String originalGeneratedPost = "";
    private String generatedSourceCitation = "";
    private String originalInputText = "";
    private boolean isEditMode = false;

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
        clearInputButton = findViewById(R.id.clearInputButton);
        editButton = findViewById(R.id.editButton);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
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
        regenerateButton.setOnClickListener(v -> onRegenerateClick());

        // Watch for text changes to show edited indicator
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

        // Update hashtags checkbox visibility
        includeHashtagsCheckbox.setVisibility(
                !prefsManager.getHashtags().isEmpty() ? View.VISIBLE : View.GONE);
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
        if (generatedSourceCitation.isEmpty())
            return;

        String currentText = outputText.getText() != null ? outputText.getText().toString() : "";
        if (show) {
            // Append if not already present
            if (!currentText.contains(generatedSourceCitation)) {
                outputText.setText(currentText + "\n\n" + generatedSourceCitation);
            }
        } else {
            // Remove if present
            if (currentText.contains(generatedSourceCitation)) {
                String newText = currentText.replace("\n\n" + generatedSourceCitation, "")
                        .replace(generatedSourceCitation, "") // Fallback
                        .trim();
                outputText.setText(newText);
            }
        }
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

        // Show progress notification
        notificationHelper.showProgressNotification(
                getString(R.string.notification_generating_title),
                getString(R.string.notification_generating_message));

        // Process in background
        String finalInput = input;
        originalInputText = input; // Store for potential regeneration
        executor.execute(() -> {
            try {
                String content = finalInput;

                // Check if input is a URL
                if (WebContentExtractor.isUrl(finalInput)) {
                    Log.i(TAG, "Input detected as URL, extracting content...");
                    mainHandler.post(() -> progressText.setText(R.string.extracting_content));
                    WebContentExtractor extractor = new WebContentExtractor();
                    content = extractor.extractContent(finalInput);
                    Log.d(TAG, "Extracted content length: " + content.length());
                } else {
                    Log.i(TAG, "Input is plain text, using directly");
                }

                // Generate post with Gemini
                mainHandler.post(() -> progressText.setText(R.string.generating_post));
                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                String tone = prefsManager.getTone();
                Log.d(TAG, "Using tone: " + tone);
                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);

                // Get source enabled state from UI
                // ALWAYS request source if feature is globally enabled, to allow dynamic
                // toggling
                boolean includeSource = prefsManager.isSourceEnabled();
                String result = gemini.curatePost(content, includeSource);

                // Update UI on main thread
                String finalResult = result;
                mainHandler.post(() -> {
                    Log.i(TAG, "Post generation SUCCESSFUL");

                    // Extract source citation
                    String contentWithoutSource = extractSourceCitation(finalResult);
                    Log.d(TAG, "Generated post length: " + contentWithoutSource.length());

                    // Set initial text (append source if checked)
                    String textToShow = contentWithoutSource;
                    if (includeSourceCheckbox.isChecked() && !generatedSourceCitation.isEmpty()) {
                        textToShow += "\n\n" + generatedSourceCitation;
                    }

                    originalGeneratedPost = textToShow;
                    setOutputText(textToShow);
                    showSkeletonLoading(false);
                    showOutputCard();
                    isEditMode = false;
                    editButton.setText(R.string.edit_button);
                    editButton.setIconResource(android.R.drawable.ic_menu_edit);
                    editedIndicator.setVisibility(View.GONE);

                    // Show refinement section after successful generation
                    refinementCard.setVisibility(View.VISIBLE);
                    clearRefinementCheckboxes();

                    // Show completion notification
                    notificationHelper.showCompletedNotification(
                            getString(R.string.notification_complete_title),
                            getString(R.string.notification_complete_message));
                });

            } catch (Exception e) {
                Log.e(TAG, "Post generation FAILED: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showSkeletonLoading(false);
                    showPlaceholder();
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(MainActivity.this,
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
     * Shows or hides the skeleton loading animation.
     */
    private void showSkeletonLoading(boolean show) {
        if (show) {
            skeletonCard.setVisibility(View.VISIBLE);
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            skeletonCard.startAnimation(slideUp);
        } else {
            skeletonCard.setVisibility(View.GONE);
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
        List<String> refinements = new ArrayList<>();

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

        // Show progress notification
        notificationHelper.showProgressNotification(
                getString(R.string.notification_generating_title),
                getString(R.string.notification_generating_message));

        // Regenerate in background
        executor.execute(() -> {
            try {
                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                String tone = prefsManager.getTone();

                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);

                // Get source enabled state from UI
                // ALWAYS request source if feature is globally enabled
                boolean includeSource = prefsManager.isSourceEnabled();
                String refinedPost = gemini.refinePost(originalGeneratedPost, refinements, includeSource);

                // Update UI on main thread
                mainHandler.post(() -> {
                    Log.i(TAG, "Post refinement SUCCESSFUL");

                    // Extract source citation
                    String contentWithoutSource = extractSourceCitation(refinedPost);

                    // Set initial text (append source if checked)
                    String textToShow = contentWithoutSource;
                    if (includeSourceCheckbox.isChecked() && !generatedSourceCitation.isEmpty()) {
                        textToShow += "\n\n" + generatedSourceCitation;
                    }

                    originalGeneratedPost = textToShow;
                    setOutputText(textToShow);
                    showSkeletonLoading(false);
                    showOutputCard();
                    isEditMode = false;
                    editButton.setText(R.string.edit_button);
                    editButton.setIconResource(android.R.drawable.ic_menu_edit);
                    editedIndicator.setVisibility(View.GONE);

                    // Show refinement section again
                    refinementCard.setVisibility(View.VISIBLE);
                    clearRefinementCheckboxes();

                    // Show completion notification
                    notificationHelper.showCompletedNotification(
                            getString(R.string.notification_complete_title),
                            getString(R.string.notification_complete_message));
                });

            } catch (Exception e) {
                Log.e(TAG, "Post refinement FAILED: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showSkeletonLoading(false);
                    outputCard.setVisibility(View.VISIBLE);
                    refinementCard.setVisibility(View.VISIBLE);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(MainActivity.this,
                            "Refinement error: " + errorMsg,
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
        executor.shutdown();
    }
}
