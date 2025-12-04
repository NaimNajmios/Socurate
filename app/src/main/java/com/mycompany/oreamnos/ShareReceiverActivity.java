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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.mycompany.oreamnos.services.GeminiService;
import com.mycompany.oreamnos.services.WebContentExtractor;
import com.mycompany.oreamnos.utils.PreferencesManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that handles shared content from other apps.
 * Receives text/URLs via share intent and processes them automatically.
 */
public class ShareReceiverActivity extends AppCompatActivity {

    private TextView sharedText;
    private TextView outputText;
    private TextView skeletonLoadingText;
    private MaterialCardView resultCard;
    private MaterialCardView skeletonCard;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private MaterialButton openMainAppButton;

    private PreferencesManager prefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String lastGeneratedPost = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize preferences
        prefsManager = new PreferencesManager(this);

        // Apply saved theme before setContentView
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_share_receiver);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        sharedText = findViewById(R.id.sharedText);
        outputText = findViewById(R.id.outputText);
        resultCard = findViewById(R.id.resultCard);
        skeletonCard = findViewById(R.id.skeletonCard);
        skeletonLoadingText = findViewById(R.id.skeletonLoadingText);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        openMainAppButton = findViewById(R.id.openMainAppButton);

        // Setup button listeners
        copyButton.setOnClickListener(v -> onCopyClick());
        shareButton.setOnClickListener(v -> onShareClick());
        openMainAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("shared_text", getSharedContent());
            startActivity(intent);
            finish();
        });

        // Process the shared content
        processSharedContent();
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

        // Display preview
        sharedText.setText(content);

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
                String result = gemini.curatePost(textToProcess);

                // Update UI on main thread
                String finalResult = result;
                mainHandler.post(() -> {
                    // Add hashtags if enabled
                    String postWithHashtags = finalResult;
                    if (prefsManager.areHashtagsEnabled()) {
                        String hashtags = prefsManager.getFormattedHashtags();
                        if (!hashtags.isEmpty()) {
                            postWithHashtags = finalResult + "\n\n" + hashtags;
                        }
                    }

                    lastGeneratedPost = postWithHashtags;
                    outputText.setText(postWithHashtags);
                    resultCard.setVisibility(View.VISIBLE);
                    showSkeleton(false);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showSkeleton(false);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(ShareReceiverActivity.this,
                            getString(R.string.processing_error, errorMsg),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Handles the copy button click.
     */
    private void onCopyClick() {
        if (lastGeneratedPost.isEmpty()) {
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Oreamnos Post", lastGeneratedPost);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the share button click.
     */
    private void onShareClick() {
        if (lastGeneratedPost.isEmpty()) {
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, lastGeneratedPost);
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
