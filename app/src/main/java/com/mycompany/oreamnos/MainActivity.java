package com.mycompany.oreamnos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mycompany.oreamnos.services.GeminiService;
import com.mycompany.oreamnos.services.WebContentExtractor;
import com.mycompany.oreamnos.utils.PreferencesManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity for the Oreamnos app.
 * Allows users to input text/URLs and generate Malaysian Malay football posts.
 */
public class MainActivity extends AppCompatActivity {

    private TextInputEditText inputText;
    private TextView outputText;
    private TextView progressText;
    private MaterialCardView outputCard;
    private View progressOverlay;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private ExtendedFloatingActionButton generateFab;

    private PreferencesManager prefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String lastGeneratedPost = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize preferences
        prefsManager = new PreferencesManager(this);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        outputCard = findViewById(R.id.outputCard);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressText = findViewById(R.id.progressText);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        generateFab = findViewById(R.id.generateFab);

        // Setup button listeners
        generateFab.setOnClickListener(v -> onGenerateClick());
        copyButton.setOnClickListener(v -> onCopyClick());
        shareButton.setOnClickListener(v -> onShareClick());

        // Check if API key is set, if not prompt user
        if (!prefsManager.hasApiKey()) {
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_LONG).show();
        }
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
     * Handles the generate button click.
     */
    private void onGenerateClick() {
        String input = inputText.getText() != null ? inputText.getText().toString().trim() : "";

        if (input.isEmpty()) {
            Toast.makeText(this, R.string.input_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if API key is configured
        if (!prefsManager.hasApiKey()) {
            Toast.makeText(this, R.string.api_key_required, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        // Show progress
        showProgress(true);
        progressText.setText(R.string.processing);

        // Process in background
        String finalInput = input;
        executor.execute(() -> {
            try {
                String content = finalInput;

                // Check if input is a URL
                if (WebContentExtractor.isUrl(finalInput)) {
                    mainHandler.post(() -> progressText.setText(R.string.extracting_content));
                    WebContentExtractor extractor = new WebContentExtractor();
                    content = extractor.extractContent(finalInput);
                }

                // Generate post with Gemini
                mainHandler.post(() -> progressText.setText(R.string.generating_post));
                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                String tone = prefsManager.getTone();

                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);
                String result = gemini.curatePost(content);

                // Update UI on main thread
                String finalResult = result;
                mainHandler.post(() -> {
                    lastGeneratedPost = finalResult;
                    outputText.setText(finalResult);
                    outputCard.setVisibility(View.VISIBLE);
                    showProgress(false);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showProgress(false);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(MainActivity.this,
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
     * Shows or hides the progress overlay.
     */
    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
