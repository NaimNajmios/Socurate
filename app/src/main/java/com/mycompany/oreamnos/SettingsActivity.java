package com.mycompany.oreamnos;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.mycompany.oreamnos.services.GeminiService;
import com.mycompany.oreamnos.utils.PreferencesManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings activity for configuring API key, tone, and endpoint.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private TextInputEditText apiKeyInput;
    private TextInputEditText endpointInput;
    private TextInputEditText hashtagsInput;
    private RadioGroup toneRadioGroup;
    private SwitchMaterial enableHashtagsSwitch;
    private MaterialButton testConnectionButton;
    private MaterialButton resetEndpointButton;
    private ExtendedFloatingActionButton saveFab;

    private PreferencesManager prefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "=== SettingsActivity onCreate ===");
        setContentView(R.layout.activity_settings);

        // Initialize preferences
        prefsManager = new PreferencesManager(this);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        apiKeyInput = findViewById(R.id.apiKeyInput);
        endpointInput = findViewById(R.id.endpointInput);
        hashtagsInput = findViewById(R.id.hashtagsInput);
        toneRadioGroup = findViewById(R.id.toneRadioGroup);
        enableHashtagsSwitch = findViewById(R.id.enableHashtagsSwitch);
        testConnectionButton = findViewById(R.id.testConnectionButton);
        resetEndpointButton = findViewById(R.id.resetEndpointButton);
        saveFab = findViewById(R.id.saveFab);

        // Load current settings
        loadSettings();

        // Setup button listeners
        saveFab.setOnClickListener(v -> onSaveClick());
        testConnectionButton.setOnClickListener(v -> onTestConnectionClick());
        resetEndpointButton.setOnClickListener(v -> {
            endpointInput.setText(R.string.api_endpoint_default);
        });
    }

    /**
     * Loads current settings from preferences.
     */
    private void loadSettings() {
        Log.d(TAG, "Loading settings from preferences");
        // Load API key (if exists)
        String apiKey = prefsManager.getApiKey();
        if (apiKey != null) {
            apiKeyInput.setText(apiKey);
        }

        // Load endpoint
        String endpoint = prefsManager.getApiEndpoint();
        endpointInput.setText(endpoint);

        // Load tone
        String tone = prefsManager.getTone();
        if (PreferencesManager.TONE_FORMAL.equals(tone)) {
            toneRadioGroup.check(R.id.toneFormal);
        } else {
            toneRadioGroup.check(R.id.toneCasual);
        }

        // Load hashtags
        String hashtags = prefsManager.getHashtags();
        hashtagsInput.setText(hashtags);

        // Load hashtags enabled state
        boolean hashtagsEnabled = prefsManager.areHashtagsEnabled();
        enableHashtagsSwitch.setChecked(hashtagsEnabled);

        Log.d(TAG, "Settings loaded - Tone: " + tone + ", Hashtags enabled: " + hashtagsEnabled);
    }

    /**
     * Saves settings to preferences.
     */
    private void onSaveClick() {
        Log.i(TAG, ">>> Save settings clicked <<<");
        // Get values
        String apiKey = apiKeyInput.getText() != null ? apiKeyInput.getText().toString().trim() : "";
        String endpoint = endpointInput.getText() != null ? endpointInput.getText().toString().trim() : "";

        // Validate
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "API key cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endpoint.isEmpty()) {
            Toast.makeText(this, "Endpoint cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get tone
        String tone = toneRadioGroup.getCheckedRadioButtonId() == R.id.toneFormal
                ? PreferencesManager.TONE_FORMAL
                : PreferencesManager.TONE_CASUAL;

        // Get hashtags
        String hashtags = hashtagsInput.getText() != null ? hashtagsInput.getText().toString().trim() : "";
        boolean hashtagsEnabled = enableHashtagsSwitch.isChecked();

        // Save
        prefsManager.saveApiKey(apiKey);
        prefsManager.saveApiEndpoint(endpoint);
        prefsManager.saveTone(tone);
        prefsManager.saveHashtags(hashtags);
        prefsManager.setHashtagsEnabled(hashtagsEnabled);

        Log.i(TAG, "Settings saved - Tone: " + tone + ", Hashtags: '" + hashtags + "', Enabled: " + hashtagsEnabled);

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Tests the API connection.
     */
    private void onTestConnectionClick() {
        Log.i(TAG, ">>> Test connection clicked <<<");
        String apiKey = apiKeyInput.getText() != null ? apiKeyInput.getText().toString().trim() : "";
        String endpoint = endpointInput.getText() != null ? endpointInput.getText().toString().trim() : "";

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter an API key first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button during test
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("Testing...");

        executor.execute(() -> {
            try {
                String tone = toneRadioGroup.getCheckedRadioButtonId() == R.id.toneFormal
                        ? PreferencesManager.TONE_FORMAL
                        : PreferencesManager.TONE_CASUAL;

                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);
                String result = gemini.curatePost("Test connection: Manchester United won 3-0.");

                Log.i(TAG, "Test connection SUCCESSFUL - Response received");
                mainHandler.post(() -> {
                    // Save the working API key
                    prefsManager.saveApiKey(apiKey);

                    testConnectionButton.setEnabled(true);
                    testConnectionButton.setText(R.string.test_connection);
                    Toast.makeText(SettingsActivity.this,
                            "Connection successful! API key saved.",
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                Log.e(TAG, "Test connection FAILED: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    testConnectionButton.setEnabled(true);
                    testConnectionButton.setText(R.string.test_connection);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(SettingsActivity.this,
                            getString(R.string.connection_test_failed, errorMsg),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "=== SettingsActivity onDestroy ===");
        super.onDestroy();
        executor.shutdown();
    }
}
