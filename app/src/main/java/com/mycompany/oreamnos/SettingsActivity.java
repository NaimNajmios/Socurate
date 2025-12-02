package com.mycompany.oreamnos;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mycompany.oreamnos.services.GeminiService;
import com.mycompany.oreamnos.utils.PreferencesManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings activity for configuring API key, tone, and endpoint.
 */
public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText apiKeyInput;
    private TextInputEditText endpointInput;
    private RadioGroup toneRadioGroup;
    private MaterialButton testConnectionButton;
    private MaterialButton resetEndpointButton;
    private ExtendedFloatingActionButton saveFab;

    private PreferencesManager prefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        toneRadioGroup = findViewById(R.id.toneRadioGroup);
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
    }

    /**
     * Saves settings to preferences.
     */
    private void onSaveClick() {
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

        // Save
        prefsManager.saveApiKey(apiKey);
        prefsManager.saveApiEndpoint(endpoint);
        prefsManager.saveTone(tone);

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Tests the API connection.
     */
    private void onTestConnectionClick() {
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

                mainHandler.post(() -> {
                    testConnectionButton.setEnabled(true);
                    testConnectionButton.setText(R.string.test_connection);
                    Toast.makeText(SettingsActivity.this,
                            R.string.connection_test_success,
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
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
        super.onDestroy();
        executor.shutdown();
    }
}
