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

    // Available Gemini models
    private static final String[] MODEL_NAMES = {
            "Gemini 2.5 Flash Lite",
            "Gemini 2.5 Flash",
            "Gemini 2.5 Pro",
            "Gemini 2.0 Flash"
    };

    private static final String[] MODEL_ENDPOINTS = {
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    };

    private TextInputEditText apiKeyInput;
    private AutoCompleteTextView modelDropdown;
    private TextInputEditText hashtagsInput;
    private RadioGroup toneRadioGroup;
    private RadioGroup themeRadioGroup;
    private SwitchMaterial enableHashtagsSwitch;
    private SwitchMaterial sourceEnabledSwitch;
    private MaterialButton testConnectionButton;

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

    private int selectedModelIndex = 3; // Default to Gemini 2.0 Flash

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
        modelDropdown = findViewById(R.id.modelDropdown);
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

        // Setup model dropdown
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
        // API Key - save on focus lost
        apiKeyInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isLoading) {
                saveApiKey();
            }
        });

        // Model dropdown - save on selection
        modelDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedModelIndex = position;
            Log.d(TAG, "Selected model: " + MODEL_NAMES[position]);
            if (!isLoading) {
                prefsManager.saveApiEndpoint(MODEL_ENDPOINTS[selectedModelIndex]);
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
     * Saves the API key.
     */
    private void saveApiKey() {
        String apiKey = apiKeyInput.getText() != null ? apiKeyInput.getText().toString().trim() : "";
        if (!apiKey.isEmpty()) {
            prefsManager.saveApiKey(apiKey);
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
     * Sets up the model dropdown with available options.
     */
    private void setupModelDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                MODEL_NAMES);
        modelDropdown.setAdapter(adapter);
        // Item click listener is now in setupAutoSaveListeners()
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

        // Load model - find matching endpoint
        String savedEndpoint = prefsManager.getApiEndpoint();
        selectedModelIndex = 3; // Default to Gemini 2.0 Flash
        for (int i = 0; i < MODEL_ENDPOINTS.length; i++) {
            if (MODEL_ENDPOINTS[i].equals(savedEndpoint)) {
                selectedModelIndex = i;
                break;
            }
        }
        modelDropdown.setText(MODEL_NAMES[selectedModelIndex], false);

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

        Log.d(TAG, "Settings loaded - Model: " + MODEL_NAMES[selectedModelIndex] + ", Tone: " + tone +
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
        String apiKey = apiKeyInput.getText() != null ? apiKeyInput.getText().toString().trim() : "";
        String endpoint = MODEL_ENDPOINTS[selectedModelIndex];

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
                String result = gemini.curatePost("Test connection: Manchester United won 3-0.", true);

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
