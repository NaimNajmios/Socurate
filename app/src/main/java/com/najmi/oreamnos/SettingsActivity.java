package com.najmi.oreamnos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.najmi.oreamnos.adapters.PillAdapter;
import com.najmi.oreamnos.model.GenerationPill;
import com.najmi.oreamnos.model.UsageStats;
import com.najmi.oreamnos.services.GeminiService;
import com.najmi.oreamnos.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings activity for configuring API key, tone, and model selection.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // Available Gemini models (free tier)
    private static final String[] GEMINI_MODEL_NAMES = {
            "Gemini 2.5 Flash Lite",
            "Gemini 2.5 Flash",
            "Gemini 2.0 Flash",
            "Gemini 2.0 Flash Lite",
            "Gemini 1.5 Flash",
            "Gemini 1.5 Flash-8B"
    };

    private static final String[] GEMINI_MODEL_ENDPOINTS = {
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-8b:generateContent"
    };

    // Available Groq models (free tier)
    private static final String[] GROQ_MODEL_NAMES = {
            "Llama 3.3 70B Versatile",
            "Llama 3.1 8B Instant",
            "Llama 3 70B",
            "Llama 3 8B",
            "Gemma 2 9B",
            "Mixtral 8x7B"
    };

    private static final String[] GROQ_MODEL_IDS = {
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "llama3-70b-8192",
            "llama3-8b-8192",
            "gemma2-9b-it",
            "mixtral-8x7b-32768"
    };

    // Available OpenRouter free models
    private static final String[] OPENROUTER_MODEL_NAMES = {
            "DeepSeek V3 Base (Free)",
            "DeepSeek Chat V3 (Free)",
            "Llama 3.3 70B Instruct (Free)",
            "Gemma 3 27B (Free)",
            "Mistral Small 3.1 24B (Free)",
            "Qwen 3 235B A22B (Free)",
            "Gemini 2.0 Flash Exp (Free)",
            "Phi-4 Multimodal (Free)",
            "Llama 4 Maverick (Free)"
    };

    private static final String[] OPENROUTER_MODEL_IDS = {
            "deepseek/deepseek-v3-base:free",
            "deepseek/deepseek-chat-v3-0324:free",
            "meta-llama/llama-3.3-70b-instruct:free",
            "google/gemma-3-27b-it:free",
            "mistralai/mistral-small-3.1-24b-instruct:free",
            "qwen/qwen3-235b-a22b:free",
            "google/gemini-2.0-flash-exp:free",
            "microsoft/phi-4-multimodal-instruct:free",
            "meta-llama/llama-4-maverick:free"
    };

    // Current model arrays (dynamically updated based on provider)
    private String[] currentModelNames = GEMINI_MODEL_NAMES;
    private String[] currentModelIds = GEMINI_MODEL_ENDPOINTS;

    private TextInputEditText apiKeyInput;
    private TextInputEditText groqApiKeyInput;
    private TextInputEditText openRouterApiKeyInput;
    private AutoCompleteTextView modelDropdown;
    private AutoCompleteTextView providerDropdown;
    private View geminiKeyContainer;
    private View groqKeyContainer;
    private View openRouterKeyContainer;
    private TextInputEditText hashtagsInput;
    private RadioGroup toneRadioGroup;
    private RadioGroup themeRadioGroup;
    private SwitchMaterial enableHashtagsSwitch;
    private SwitchMaterial sourceEnabledSwitch;
    private MaterialButton testConnectionButton;

    // Provider constants (must match PreferencesManager)
    private static final String[] PROVIDER_NAMES = { "Gemini", "Groq", "OpenRouter" };
    private static final String[] PROVIDER_VALUES = { "gemini", "groq", "openrouter" };

    // Flag to prevent auto-save during initial load
    private boolean isLoading = true;

    // Pills section
    private RecyclerView pillsRecyclerView;
    private TextView pillsEmptyText;
    private MaterialButton addPillButton;
    private PillAdapter pillAdapter;

    // Usage stats section
    private TextView totalTokensValue;
    private TextView promptTokensValue;
    private TextView responseTokensValue;
    private View promptTokensBar;
    private View responseTokensBar;
    private TextView successfulRequestsValue;
    private TextView failedRequestsValue;
    private MaterialButton resetStatsButton;

    private PreferencesManager prefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int selectedModelIndex = 0; // Default to first model

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "=== SettingsActivity onCreate ===");

        // Initialize preferences
        prefsManager = new PreferencesManager(this);

        // Apply saved theme before setContentView
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_settings);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        apiKeyInput = findViewById(R.id.apiKeyInput);
        groqApiKeyInput = findViewById(R.id.groqApiKeyInput);
        openRouterApiKeyInput = findViewById(R.id.openRouterApiKeyInput);
        modelDropdown = findViewById(R.id.modelDropdown);
        providerDropdown = findViewById(R.id.providerDropdown);
        geminiKeyContainer = findViewById(R.id.geminiKeyContainer);
        groqKeyContainer = findViewById(R.id.groqKeyContainer);
        openRouterKeyContainer = findViewById(R.id.openRouterKeyContainer);
        hashtagsInput = findViewById(R.id.hashtagsInput);
        toneRadioGroup = findViewById(R.id.toneRadioGroup);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        enableHashtagsSwitch = findViewById(R.id.enableHashtagsSwitch);
        sourceEnabledSwitch = findViewById(R.id.sourceEnabledSwitch);
        testConnectionButton = findViewById(R.id.testConnectionButton);

        // Pills section views
        pillsRecyclerView = findViewById(R.id.pillsRecyclerView);
        pillsEmptyText = findViewById(R.id.pillsEmptyText);
        addPillButton = findViewById(R.id.addPillButton);

        // Setup dropdowns
        setupProviderDropdown();
        setupModelDropdown();

        // Setup pills section
        setupPillsSection();

        // Setup usage stats section
        setupUsageStatsSection();

        // Load current settings
        loadSettings();

        // Setup auto-save listeners (after loading to prevent initial saves)
        setupAutoSaveListeners();

        // Setup button listeners
        testConnectionButton.setOnClickListener(v -> onTestConnectionClick());

        // Theme radio group listener - save immediately
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (isLoading)
                return;
            String theme;
            if (checkedId == R.id.themeLight) {
                theme = PreferencesManager.THEME_LIGHT;
            } else if (checkedId == R.id.themeDark) {
                theme = PreferencesManager.THEME_DARK;
            } else {
                theme = PreferencesManager.THEME_SYSTEM;
            }
            prefsManager.saveTheme(theme);
            applyTheme(theme);
            showSavedFeedback();
        });
    }

    /**
     * Sets up auto-save listeners for all input fields.
     */
    private void setupAutoSaveListeners() {
        // Gemini API Key - save on focus lost
        apiKeyInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isLoading) {
                saveGeminiApiKey();
            }
        });

        // Groq API Key - save on focus lost
        groqApiKeyInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isLoading) {
                saveGroqApiKey();
            }
        });

        // OpenRouter API Key - save on focus lost
        openRouterApiKeyInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isLoading) {
                saveOpenRouterApiKey();
            }
        });

        // Model dropdown - save on selection
        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedModelIndex = position;
            Log.d(TAG, "Selected model: " + currentModelNames[position]);
            if (!isLoading) {
                String provider = prefsManager.getProvider();
                prefsManager.saveModelForProvider(provider, currentModelIds[selectedModelIndex]);
                showSavedFeedback();
            }
        });

        // Tone radio group - save on change
        toneRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (isLoading)
                return;
            String tone = checkedId == R.id.toneFormal
                    ? PreferencesManager.TONE_FORMAL
                    : PreferencesManager.TONE_CASUAL;
            prefsManager.saveTone(tone);
            showSavedFeedback();
        });

        // Hashtags - save on text change with debounce
        hashtagsInput.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable saveRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isLoading)
                    return;
                if (saveRunnable != null) {
                    handler.removeCallbacks(saveRunnable);
                }
                saveRunnable = () -> {
                    String hashtags = s.toString().trim();
                    prefsManager.saveHashtags(hashtags);
                    Log.d(TAG, "Auto-saved hashtags: " + hashtags);
                };
                handler.postDelayed(saveRunnable, 500); // 500ms debounce
            }
        });

        // Hashtags enabled switch - save on change
        enableHashtagsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoading)
                return;
            prefsManager.setHashtagsEnabled(isChecked);
            showSavedFeedback();
        });

        // Source enabled switch - save on change
        sourceEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoading)
                return;
            prefsManager.saveSourceEnabled(isChecked);
            showSavedFeedback();
        });
    }

    /**
     * Saves the Gemini API key.
     */
    private void saveGeminiApiKey() {
        String apiKey = apiKeyInput.getText() != null ? apiKeyInput.getText().toString().trim() : "";
        if (!apiKey.isEmpty()) {
            prefsManager.saveApiKey(apiKey);
            showSavedFeedback();
        }
    }

    /**
     * Saves the Groq API key.
     */
    private void saveGroqApiKey() {
        String apiKey = groqApiKeyInput.getText() != null ? groqApiKeyInput.getText().toString().trim() : "";
        if (!apiKey.isEmpty()) {
            prefsManager.saveGroqApiKey(apiKey);
            showSavedFeedback();
        }
    }

    /**
     * Saves the OpenRouter API key.
     */
    private void saveOpenRouterApiKey() {
        String apiKey = openRouterApiKeyInput.getText() != null ? openRouterApiKeyInput.getText().toString().trim()
                : "";
        if (!apiKey.isEmpty()) {
            prefsManager.saveOpenRouterApiKey(apiKey);
            showSavedFeedback();
        }
    }

    /**
     * Shows brief feedback when settings are auto-saved.
     */
    private void showSavedFeedback() {
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sets up the AI provider dropdown.
     */
    private void setupProviderDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                PROVIDER_NAMES);
        providerDropdown.setAdapter(adapter);

        // Handle provider selection
        providerDropdown.setOnItemClickListener((parent, view, position, id) -> {
            if (isLoading)
                return;

            String selectedProvider = PROVIDER_VALUES[position];
            prefsManager.saveProvider(selectedProvider);
            updateApiKeyContainerVisibility(selectedProvider);
            updateModelDropdownForProvider(selectedProvider);
            showSavedFeedback();
            Log.d(TAG, "Selected provider: " + selectedProvider);
        });
    }

    /**
     * Updates the visibility of API key containers based on selected provider.
     */
    private void updateApiKeyContainerVisibility(String provider) {
        geminiKeyContainer.setVisibility(View.GONE);
        groqKeyContainer.setVisibility(View.GONE);
        openRouterKeyContainer.setVisibility(View.GONE);

        switch (provider) {
            case PreferencesManager.PROVIDER_GROQ:
                groqKeyContainer.setVisibility(View.VISIBLE);
                break;
            case PreferencesManager.PROVIDER_OPENROUTER:
                openRouterKeyContainer.setVisibility(View.VISIBLE);
                break;
            case PreferencesManager.PROVIDER_GEMINI:
            default:
                geminiKeyContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Sets up the model dropdown with available options.
     */
    private void setupModelDropdown() {
        // Initial setup with Gemini models (will be updated based on provider)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                currentModelNames);
        modelDropdown.setAdapter(adapter);
        // Item click listener is now in setupAutoSaveListeners()
    }

    /**
     * Updates the model dropdown based on the selected provider.
     */
    private void updateModelDropdownForProvider(String provider) {
        switch (provider) {
            case PreferencesManager.PROVIDER_GROQ:
                currentModelNames = GROQ_MODEL_NAMES;
                currentModelIds = GROQ_MODEL_IDS;
                break;
            case PreferencesManager.PROVIDER_OPENROUTER:
                currentModelNames = OPENROUTER_MODEL_NAMES;
                currentModelIds = OPENROUTER_MODEL_IDS;
                break;
            case PreferencesManager.PROVIDER_GEMINI:
            default:
                currentModelNames = GEMINI_MODEL_NAMES;
                currentModelIds = GEMINI_MODEL_ENDPOINTS;
                break;
        }

        // Update adapter with new model list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                currentModelNames);
        modelDropdown.setAdapter(adapter);

        // Load saved model for this provider or default to first
        String savedModel = prefsManager.getModelForProvider(provider);
        selectedModelIndex = 0;
        for (int i = 0; i < currentModelIds.length; i++) {
            if (currentModelIds[i].equals(savedModel)) {
                selectedModelIndex = i;
                break;
            }
        }
        modelDropdown.setText(currentModelNames[selectedModelIndex], false);
        Log.d(TAG, "Updated model dropdown for provider: " + provider + ", selected: "
                + currentModelNames[selectedModelIndex]);
    }

    /**
     * Loads current settings from preferences.
     */
    private void loadSettings() {
        Log.d(TAG, "Loading settings from preferences");

        // Load provider
        String provider = prefsManager.getProvider();
        int providerIndex = 0;
        for (int i = 0; i < PROVIDER_VALUES.length; i++) {
            if (PROVIDER_VALUES[i].equals(provider)) {
                providerIndex = i;
                break;
            }
        }
        providerDropdown.setText(PROVIDER_NAMES[providerIndex], false);
        updateApiKeyContainerVisibility(provider);

        // Load API keys for all providers
        String geminiKey = prefsManager.getApiKey();
        if (geminiKey != null) {
            apiKeyInput.setText(geminiKey);
        }

        String groqKey = prefsManager.getGroqApiKey();
        if (groqKey != null) {
            groqApiKeyInput.setText(groqKey);
        }

        String openRouterKey = prefsManager.getOpenRouterApiKey();
        if (openRouterKey != null) {
            openRouterApiKeyInput.setText(openRouterKey);
        }

        // Update model dropdown for current provider
        updateModelDropdownForProvider(provider);

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

        // Load source citation enabled state
        boolean sourceEnabled = prefsManager.isSourceEnabled();
        sourceEnabledSwitch.setChecked(sourceEnabled);

        // Load theme
        String theme = prefsManager.getTheme();
        if (PreferencesManager.THEME_LIGHT.equals(theme)) {
            themeRadioGroup.check(R.id.themeLight);
        } else if (PreferencesManager.THEME_DARK.equals(theme)) {
            themeRadioGroup.check(R.id.themeDark);
        } else {
            themeRadioGroup.check(R.id.themeSystem);
        }

        Log.d(TAG, "Settings loaded - Model: " + currentModelNames[selectedModelIndex] + ", Tone: " + tone +
                ", Theme: " + theme + ", Hashtags enabled: " + hashtagsEnabled +
                ", Source enabled: " + sourceEnabled);

        // Mark loading complete so listeners can save
        isLoading = false;
    }

    /**
     * Tests the API connection.
     */
    private void onTestConnectionClick() {
        Log.i(TAG, ">>> Test connection clicked <<<");
        String provider = prefsManager.getProvider();
        String apiKey;

        // Get the correct API key for the current provider
        switch (provider) {
            case PreferencesManager.PROVIDER_GROQ:
                apiKey = groqApiKeyInput.getText() != null ? groqApiKeyInput.getText().toString().trim() : "";
                break;
            case PreferencesManager.PROVIDER_OPENROUTER:
                apiKey = openRouterApiKeyInput.getText() != null ? openRouterApiKeyInput.getText().toString().trim()
                        : "";
                break;
            case PreferencesManager.PROVIDER_GEMINI:
            default:
                apiKey = this.apiKeyInput.getText() != null ? this.apiKeyInput.getText().toString().trim() : "";
                break;
        }

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter an API key first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button during test
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("Testing...");

        final String finalApiKey = apiKey;
        executor.execute(() -> {
            try {
                String tone = toneRadioGroup.getCheckedRadioButtonId() == R.id.toneFormal
                        ? PreferencesManager.TONE_FORMAL
                        : PreferencesManager.TONE_CASUAL;

                String result;
                switch (provider) {
                    case PreferencesManager.PROVIDER_GROQ:
                        // Use OpenAICompatibleCurator for Groq
                        com.najmi.oreamnos.curator.OpenAICompatibleCurator groqCurator = new com.najmi.oreamnos.curator.OpenAICompatibleCurator(
                                finalApiKey,
                                "https://api.groq.com/openai/v1/chat/completions",
                                currentModelIds[selectedModelIndex],
                                tone,
                                false);
                        result = groqCurator.curatePost("Test connection: Manchester United won 3-0.", true);
                        break;
                    case PreferencesManager.PROVIDER_OPENROUTER:
                        // Use OpenAICompatibleCurator for OpenRouter
                        com.najmi.oreamnos.curator.OpenAICompatibleCurator openRouterCurator = new com.najmi.oreamnos.curator.OpenAICompatibleCurator(
                                finalApiKey,
                                "https://openrouter.ai/api/v1/chat/completions",
                                currentModelIds[selectedModelIndex],
                                tone,
                                true);
                        result = openRouterCurator.curatePost("Test connection: Manchester United won 3-0.", true);
                        break;
                    case PreferencesManager.PROVIDER_GEMINI:
                    default:
                        // Use GeminiService for Gemini
                        String endpoint = currentModelIds[selectedModelIndex];
                        GeminiService gemini = new GeminiService(finalApiKey, endpoint, tone);
                        result = gemini.curatePost("Test connection: Manchester United won 3-0.", true);
                        break;
                }

                Log.i(TAG, "Test connection SUCCESSFUL - Response received");
                mainHandler.post(() -> {
                    // Save the working API key for the current provider
                    switch (provider) {
                        case PreferencesManager.PROVIDER_GROQ:
                            prefsManager.saveGroqApiKey(finalApiKey);
                            break;
                        case PreferencesManager.PROVIDER_OPENROUTER:
                            prefsManager.saveOpenRouterApiKey(finalApiKey);
                            break;
                        case PreferencesManager.PROVIDER_GEMINI:
                        default:
                            prefsManager.saveApiKey(finalApiKey);
                            break;
                    }

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

    // ==================== PILLS MANAGEMENT ====================

    /**
     * Sets up the pills section with RecyclerView and adapter.
     */
    private void setupPillsSection() {
        pillAdapter = new PillAdapter(new PillAdapter.OnPillActionListener() {
            @Override
            public void onPillClick(GenerationPill pill) {
                showPillDialog(pill);
            }

            @Override
            public void onPillEdit(GenerationPill pill) {
                showPillDialog(pill);
            }

            @Override
            public void onPillDelete(GenerationPill pill) {
                confirmDeletePill(pill);
            }

            @Override
            public void onPillSetActive(GenerationPill pill) {
                String currentActive = prefsManager.getActivePillId();
                if (pill.getId().equals(currentActive)) {
                    // Toggle off if already active
                    prefsManager.saveActivePillId(null);
                    Toast.makeText(SettingsActivity.this, R.string.pill_cleared, Toast.LENGTH_SHORT).show();
                } else {
                    prefsManager.saveActivePillId(pill.getId());
                    Toast.makeText(SettingsActivity.this, R.string.pill_set_active, Toast.LENGTH_SHORT).show();
                }
                refreshPills();
            }
        });

        pillsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pillsRecyclerView.setAdapter(pillAdapter);

        // Add pill button
        addPillButton.setOnClickListener(v -> showPillDialog(null));

        // Initial load
        refreshPills();
    }

    // ==================== USAGE STATS MANAGEMENT ====================

    /**
     * Sets up the usage stats section.
     */
    private void setupUsageStatsSection() {
        totalTokensValue = findViewById(R.id.totalTokensValue);
        promptTokensValue = findViewById(R.id.promptTokensValue);
        responseTokensValue = findViewById(R.id.responseTokensValue);
        promptTokensBar = findViewById(R.id.promptTokensBar);
        responseTokensBar = findViewById(R.id.responseTokensBar);
        successfulRequestsValue = findViewById(R.id.successfulRequestsValue);
        failedRequestsValue = findViewById(R.id.failedRequestsValue);
        resetStatsButton = findViewById(R.id.resetStatsButton);

        // Load current stats
        refreshUsageStats();

        // Reset button click listener
        resetStatsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Statistics")
                    .setMessage("Are you sure you want to reset all usage statistics?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        prefsManager.resetUsageStats();
                        refreshUsageStats();
                        Toast.makeText(this, R.string.stats_reset, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    /**
     * Refreshes the usage stats display.
     */
    private void refreshUsageStats() {
        UsageStats stats = prefsManager.getUsageStats();

        // Update total tokens (large display)
        totalTokensValue.setText(String.format("%,d", stats.getTotalTokens()));

        // Update legend text
        promptTokensValue.setText(String.format("Prompt: %,d", stats.getTotalPromptTokens()));
        responseTokensValue.setText(String.format("Response: %,d", stats.getTotalCandidateTokens()));

        // Update bar weights for visualization
        long promptTokens = stats.getTotalPromptTokens();
        long responseTokens = stats.getTotalCandidateTokens();
        long totalTokens = promptTokens + responseTokens;

        if (totalTokens > 0) {
            float promptWeight = (float) promptTokens / totalTokens;
            float responseWeight = (float) responseTokens / totalTokens;

            android.widget.LinearLayout.LayoutParams promptParams = (android.widget.LinearLayout.LayoutParams) promptTokensBar
                    .getLayoutParams();
            promptParams.weight = promptWeight;
            promptTokensBar.setLayoutParams(promptParams);

            android.widget.LinearLayout.LayoutParams responseParams = (android.widget.LinearLayout.LayoutParams) responseTokensBar
                    .getLayoutParams();
            responseParams.weight = responseWeight;
            responseTokensBar.setLayoutParams(responseParams);
        } else {
            // Equal weights when no data
            android.widget.LinearLayout.LayoutParams promptParams = (android.widget.LinearLayout.LayoutParams) promptTokensBar
                    .getLayoutParams();
            promptParams.weight = 0.5f;
            promptTokensBar.setLayoutParams(promptParams);

            android.widget.LinearLayout.LayoutParams responseParams = (android.widget.LinearLayout.LayoutParams) responseTokensBar
                    .getLayoutParams();
            responseParams.weight = 0.5f;
            responseTokensBar.setLayoutParams(responseParams);
        }

        // Update request counters
        successfulRequestsValue.setText(String.valueOf(stats.getSuccessfulRequests()));
        failedRequestsValue.setText(String.valueOf(stats.getFailedRequests()));
    }

    /**
     * Refreshes the pills list from preferences.
     */
    private void refreshPills() {
        List<GenerationPill> pills = prefsManager.getPills();
        pillAdapter.setPills(pills);
        pillAdapter.setActivePillId(prefsManager.getActivePillId());

        // Show/hide empty state
        if (pills.isEmpty()) {
            pillsEmptyText.setVisibility(View.VISIBLE);
            pillsRecyclerView.setVisibility(View.GONE);
        } else {
            pillsEmptyText.setVisibility(View.GONE);
            pillsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows the create/edit pill dialog.
     */
    private void showPillDialog(GenerationPill existingPill) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_pill, null);
        dialog.setContentView(dialogView);

        // Get views
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        TextInputEditText nameInput = dialogView.findViewById(R.id.pillNameInput);
        RadioGroup toneRadioGroup = dialogView.findViewById(R.id.toneRadioGroup);
        Chip chipRephrase = dialogView.findViewById(R.id.chipRephrase);
        Chip chipRecheckFlow = dialogView.findViewById(R.id.chipRecheckFlow);
        Chip chipRecheckWording = dialogView.findViewById(R.id.chipRecheckWording);
        Chip chipShortenDetailed = dialogView.findViewById(R.id.chipShortenDetailed);
        TextInputEditText customInstructionInput = dialogView.findViewById(R.id.customInstructionInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        // Setup for edit mode
        final GenerationPill pill;
        if (existingPill != null) {
            pill = existingPill;
            titleText.setText(R.string.edit_pill);
            nameInput.setText(pill.getName());

            // Set tone
            if ("casual".equals(pill.getTone())) {
                toneRadioGroup.check(R.id.toneCasual);
            } else {
                toneRadioGroup.check(R.id.toneFormal);
            }

            // Set refinements
            List<String> refinements = pill.getRefinements();
            chipRephrase.setChecked(refinements.contains("rephrase"));
            chipRecheckFlow.setChecked(refinements.contains("recheck_flow"));
            chipRecheckWording.setChecked(refinements.contains("recheck_wording"));
            chipShortenDetailed.setChecked(refinements.contains("shorten_detailed"));

            // Set custom instruction
            customInstructionInput.setText(pill.getCustomInstruction());
        } else {
            pill = new GenerationPill();
            titleText.setText(R.string.create_pill);
        }

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Save button
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

            if (name.isEmpty()) {
                nameInput.setError("Name is required");
                return;
            }

            // Get tone
            String tone = toneRadioGroup.getCheckedRadioButtonId() == R.id.toneCasual ? "casual" : "formal";

            // Get refinements
            List<String> refinements = new ArrayList<>();
            if (chipRephrase.isChecked())
                refinements.add("rephrase");
            if (chipRecheckFlow.isChecked())
                refinements.add("recheck_flow");
            if (chipRecheckWording.isChecked())
                refinements.add("recheck_wording");
            if (chipShortenDetailed.isChecked())
                refinements.add("shorten_detailed");

            // Get custom instruction
            String customInstruction = customInstructionInput.getText() != null
                    ? customInstructionInput.getText().toString().trim()
                    : "";

            // Update pill
            pill.setName(name);
            pill.setTone(tone);
            pill.setRefinements(refinements);
            pill.setCustomInstruction(customInstruction);

            // Save
            prefsManager.savePill(pill);
            Toast.makeText(this, R.string.pill_saved, Toast.LENGTH_SHORT).show();

            dialog.dismiss();
            refreshPills();
        });

        dialog.show();
    }

    /**
     * Shows confirmation dialog for deleting a pill.
     */
    private void confirmDeletePill(GenerationPill pill) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pill")
                .setMessage("Delete \"" + pill.getName() + "\"?")
                .setPositiveButton("Delete", (d, which) -> {
                    prefsManager.deletePill(pill.getId());
                    Toast.makeText(this, R.string.pill_deleted, Toast.LENGTH_SHORT).show();
                    refreshPills();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "=== SettingsActivity onDestroy ===");
        super.onDestroy();
        executor.shutdown();
    }
}
