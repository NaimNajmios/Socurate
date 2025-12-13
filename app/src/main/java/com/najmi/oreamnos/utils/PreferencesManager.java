package com.najmi.oreamnos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Manages app preferences and secure storage for sensitive data like API keys.
 * Uses EncryptedSharedPreferences for secure storage of API credentials.
 */
public class PreferencesManager {

    private static final String PREFS_FILE_NAME = "oreamnos_secure_prefs";
    private static final String KEY_API_KEY = "gemini_api_key";
    private static final String KEY_API_ENDPOINT = "api_endpoint";
    private static final String KEY_TONE = "post_tone";
    private static final String KEY_HASHTAGS = "default_hashtags";
    private static final String KEY_HASHTAGS_ENABLED = "hashtags_enabled";
    private static final String KEY_SOURCE_ENABLED = "source_enabled";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_PROVIDER = "ai_provider";
    private static final String KEY_GROQ_API_KEY = "groq_api_key";
    private static final String KEY_OPENROUTER_API_KEY = "openrouter_api_key";
    private static final String DEFAULT_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String DEFAULT_HASHTAGS = "#BolaSepak #Football";

    public static final String TONE_FORMAL = "formal";
    public static final String TONE_CASUAL = "casual";

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    // AI Provider constants
    public static final String PROVIDER_GEMINI = "gemini";
    public static final String PROVIDER_GROQ = "groq";
    public static final String PROVIDER_OPENROUTER = "openrouter";

    // Model keys per provider
    private static final String KEY_GEMINI_MODEL = "gemini_model";
    private static final String KEY_GROQ_MODEL = "groq_model";
    private static final String KEY_OPENROUTER_MODEL = "openrouter_model";

    // Default models per provider
    private static final String DEFAULT_GEMINI_MODEL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";
    private static final String DEFAULT_GROQ_MODEL = "llama-3.3-70b-versatile";
    private static final String DEFAULT_OPENROUTER_MODEL = "deepseek/deepseek-v3-base:free";

    private final SharedPreferences securePrefs;
    private final Context context;

    /**
     * Creates a new PreferencesManager instance.
     * 
     * @param context Application context
     */
    public PreferencesManager(Context context) {
        this.context = context.getApplicationContext();
        this.securePrefs = getEncryptedPreferences();
    }

    /**
     * Gets or creates encrypted shared preferences.
     * Falls back to regular SharedPreferences if encryption is not available.
     * 
     * @return SharedPreferences instance
     */
    private SharedPreferences getEncryptedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            // Fallback to regular SharedPreferences if encryption fails
            android.util.Log.w("PreferencesManager",
                    "Failed to create encrypted preferences, using regular preferences", e);
            return context.getSharedPreferences(PREFS_FILE_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    /**
     * Saves the Gemini API key securely.
     * 
     * @param apiKey The API key to save
     * @return true if saved successfully
     */
    public boolean saveApiKey(String apiKey) {
        try {
            securePrefs.edit()
                    .putString(KEY_API_KEY, apiKey)
                    .apply();
            return true;
        } catch (Exception e) {
            android.util.Log.e("PreferencesManager", "Failed to save API key", e);
            return false;
        }
    }

    /**
     * Retrieves the saved Gemini API key.
     * 
     * @return The API key, or null if not set
     */
    public String getApiKey() {
        try {
            return securePrefs.getString(KEY_API_KEY, null);
        } catch (Exception e) {
            android.util.Log.e("PreferencesManager", "Failed to retrieve API key", e);
            return null;
        }
    }

    /**
     * Checks if an API key is configured.
     * 
     * @return true if API key exists
     */
    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    /**
     * Saves the API endpoint URL.
     * 
     * @param endpoint The endpoint URL
     */
    public void saveApiEndpoint(String endpoint) {
        securePrefs.edit()
                .putString(KEY_API_ENDPOINT, endpoint)
                .apply();
    }

    /**
     * Retrieves the API endpoint URL.
     * 
     * @return The endpoint URL, or default if not set
     */
    public String getApiEndpoint() {
        return securePrefs.getString(KEY_API_ENDPOINT, DEFAULT_ENDPOINT);
    }

    /**
     * Resets the API endpoint to default.
     */
    public void resetApiEndpointToDefault() {
        saveApiEndpoint(DEFAULT_ENDPOINT);
    }

    /**
     * Saves the post tone preference.
     * 
     * @param tone Either TONE_FORMAL or TONE_CASUAL
     */
    public void saveTone(String tone) {
        securePrefs.edit()
                .putString(KEY_TONE, tone)
                .apply();
    }

    /**
     * Retrieves the post tone preference.
     * 
     * @return The tone preference, defaults to TONE_FORMAL
     */
    public String getTone() {
        return securePrefs.getString(KEY_TONE, TONE_FORMAL);
    }

    /**
     * Checks if formal tone is selected.
     * 
     * @return true if formal tone is selected
     */
    public boolean isFormalTone() {
        return TONE_FORMAL.equals(getTone());
    }

    /**
     * Saves default hashtags.
     * 
     * @param hashtags Hashtags to append (space or comma separated)
     */
    public void saveHashtags(String hashtags) {
        securePrefs.edit()
                .putString(KEY_HASHTAGS, hashtags)
                .apply();
    }

    /**
     * Retrieves default hashtags.
     * 
     * @return The hashtags, or default if not set
     */
    public String getHashtags() {
        return securePrefs.getString(KEY_HASHTAGS, DEFAULT_HASHTAGS);
    }

    /**
     * Enables or disables hashtag auto-append.
     * 
     * @param enabled true to auto-append hashtags
     */
    public void setHashtagsEnabled(boolean enabled) {
        securePrefs.edit()
                .putBoolean(KEY_HASHTAGS_ENABLED, enabled)
                .apply();
    }

    /**
     * Checks if hashtags are enabled.
     * 
     * @return true if hashtags should be auto-appended
     */
    public boolean areHashtagsEnabled() {
        return securePrefs.getBoolean(KEY_HASHTAGS_ENABLED, true); // Enabled by default
    }

    /**
     * Formats hashtags for appending to posts.
     * Ensures each hashtag starts with # and is on its own line.
     * 
     * @return Formatted hashtags ready to append
     */
    public String getFormattedHashtags() {
        String hashtags = getHashtags();
        if (hashtags == null || hashtags.trim().isEmpty()) {
            return "";
        }

        // Clean and format hashtags - each on its own line
        String[] tags = hashtags.split("[,\\s]+");
        StringBuilder formatted = new StringBuilder();

        for (String tag : tags) {
            tag = tag.trim();
            if (!tag.isEmpty()) {
                if (formatted.length() > 0) {
                    formatted.append("\n");
                }
                if (!tag.startsWith("#")) {
                    formatted.append("#");
                }
                formatted.append(tag);
            }
        }

        return formatted.toString();
    }

    /**
     * Saves the theme preference.
     * 
     * @param theme One of THEME_SYSTEM, THEME_LIGHT, or THEME_DARK
     */
    public void saveTheme(String theme) {
        securePrefs.edit()
                .putString(KEY_THEME, theme)
                .apply();
    }

    /**
     * Retrieves the theme preference.
     * 
     * @return The theme preference, defaults to THEME_SYSTEM
     */
    public String getTheme() {
        return securePrefs.getString(KEY_THEME, THEME_SYSTEM);
    }

    /**
     * Saves the source citation enabled state.
     */
    public void saveSourceEnabled(boolean enabled) {
        securePrefs.edit().putBoolean(KEY_SOURCE_ENABLED, enabled).apply();
    }

    /**
     * Gets the source citation enabled state.
     */
    public boolean isSourceEnabled() {
        return securePrefs.getBoolean(KEY_SOURCE_ENABLED, true); // Enabled by default
    }

    // ==================== AI PROVIDER ====================

    /**
     * Saves the selected AI provider.
     * 
     * @param provider One of PROVIDER_GEMINI, PROVIDER_GROQ, or PROVIDER_OPENROUTER
     */
    public void saveProvider(String provider) {
        securePrefs.edit()
                .putString(KEY_PROVIDER, provider)
                .apply();
    }

    /**
     * Gets the selected AI provider.
     * 
     * @return The provider, defaults to PROVIDER_GEMINI
     */
    public String getProvider() {
        return securePrefs.getString(KEY_PROVIDER, PROVIDER_GEMINI);
    }

    /**
     * Saves the Groq API key securely.
     */
    public boolean saveGroqApiKey(String apiKey) {
        try {
            securePrefs.edit()
                    .putString(KEY_GROQ_API_KEY, apiKey)
                    .apply();
            return true;
        } catch (Exception e) {
            android.util.Log.e("PreferencesManager", "Failed to save Groq API key", e);
            return false;
        }
    }

    /**
     * Gets the Groq API key.
     */
    public String getGroqApiKey() {
        try {
            return securePrefs.getString(KEY_GROQ_API_KEY, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves the OpenRouter API key securely.
     */
    public boolean saveOpenRouterApiKey(String apiKey) {
        try {
            securePrefs.edit()
                    .putString(KEY_OPENROUTER_API_KEY, apiKey)
                    .apply();
            return true;
        } catch (Exception e) {
            android.util.Log.e("PreferencesManager", "Failed to save OpenRouter API key", e);
            return false;
        }
    }

    /**
     * Gets the OpenRouter API key.
     */
    public String getOpenRouterApiKey() {
        try {
            return securePrefs.getString(KEY_OPENROUTER_API_KEY, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the current provider has a valid API key.
     */
    public boolean hasApiKeyForCurrentProvider() {
        String provider = getProvider();
        switch (provider) {
            case PROVIDER_GROQ:
                String groqKey = getGroqApiKey();
                return groqKey != null && !groqKey.trim().isEmpty();
            case PROVIDER_OPENROUTER:
                String orKey = getOpenRouterApiKey();
                return orKey != null && !orKey.trim().isEmpty();
            case PROVIDER_GEMINI:
            default:
                return hasApiKey();
        }
    }

    /**
     * Saves the selected model for a specific provider.
     * 
     * @param provider The provider (gemini, groq, openrouter)
     * @param modelId  The model ID or endpoint
     */
    public void saveModelForProvider(String provider, String modelId) {
        String key;
        switch (provider) {
            case PROVIDER_GROQ:
                key = KEY_GROQ_MODEL;
                break;
            case PROVIDER_OPENROUTER:
                key = KEY_OPENROUTER_MODEL;
                break;
            case PROVIDER_GEMINI:
            default:
                key = KEY_GEMINI_MODEL;
                break;
        }
        securePrefs.edit().putString(key, modelId).apply();
    }

    /**
     * Gets the selected model for a specific provider.
     * 
     * @param provider The provider (gemini, groq, openrouter)
     * @return The model ID or endpoint, or default if not set
     */
    public String getModelForProvider(String provider) {
        String key;
        String defaultModel;
        switch (provider) {
            case PROVIDER_GROQ:
                key = KEY_GROQ_MODEL;
                defaultModel = DEFAULT_GROQ_MODEL;
                break;
            case PROVIDER_OPENROUTER:
                key = KEY_OPENROUTER_MODEL;
                defaultModel = DEFAULT_OPENROUTER_MODEL;
                break;
            case PROVIDER_GEMINI:
            default:
                key = KEY_GEMINI_MODEL;
                defaultModel = DEFAULT_GEMINI_MODEL;
                break;
        }
        return securePrefs.getString(key, defaultModel);
    }

    // ==================== CUSTOM REFINEMENT PILLS ====================

    private static final String KEY_PILLS = "generation_pills";

    /**
     * Saves all custom refinement pills.
     * 
     * @param pills List of pills to save
     */
    public void savePills(java.util.List<com.najmi.oreamnos.model.GenerationPill> pills) {
        String json = com.najmi.oreamnos.model.GenerationPill.toJson(pills);
        securePrefs.edit()
                .putString(KEY_PILLS, json)
                .apply();
    }

    /**
     * Retrieves all saved custom refinement pills.
     * 
     * @return List of pills, empty list if none saved
     */
    public java.util.List<com.najmi.oreamnos.model.GenerationPill> getPills() {
        String json = securePrefs.getString(KEY_PILLS, null);
        return com.najmi.oreamnos.model.GenerationPill.fromJson(json);
    }

    /**
     * Adds a new pill or updates existing one.
     * 
     * @param pill The pill to add or update
     */
    public void savePill(com.najmi.oreamnos.model.GenerationPill pill) {
        java.util.List<com.najmi.oreamnos.model.GenerationPill> pills = getPills();

        // Check if pill exists and update it
        boolean found = false;
        for (int i = 0; i < pills.size(); i++) {
            if (pills.get(i).getId().equals(pill.getId())) {
                pills.set(i, pill);
                found = true;
                break;
            }
        }

        // Add new pill if not found
        if (!found) {
            pills.add(pill);
        }

        savePills(pills);
    }

    /**
     * Deletes a pill by ID.
     * 
     * @param pillId ID of the pill to delete
     */
    public void deletePill(String pillId) {
        java.util.List<com.najmi.oreamnos.model.GenerationPill> pills = getPills();
        pills.removeIf(p -> p.getId().equals(pillId));
        savePills(pills);
    }

    /**
     * Clears all stored preferences.
     */
    public void clearAll() {
        securePrefs.edit().clear().apply();
    }

    // ==================== USAGE STATS ====================

    private static final String KEY_USAGE_STATS = "usage_stats";

    /**
     * Gets the current usage statistics.
     */
    public com.najmi.oreamnos.model.UsageStats getUsageStats() {
        String json = securePrefs.getString(KEY_USAGE_STATS, null);
        return com.najmi.oreamnos.model.UsageStats.fromJson(json);
    }

    /**
     * Saves usage statistics.
     */
    public void saveUsageStats(com.najmi.oreamnos.model.UsageStats stats) {
        securePrefs.edit()
                .putString(KEY_USAGE_STATS, stats.toJson())
                .apply();
    }

    /**
     * Records a successful API call with token usage and provider/model info.
     */
    public void recordApiSuccess(int promptTokens, int candidateTokens, int totalTokens,
            String provider, String modelId, String modelName) {
        com.najmi.oreamnos.model.UsageStats stats = getUsageStats();
        stats.recordSuccess(promptTokens, candidateTokens, totalTokens, provider, modelId, modelName);
        saveUsageStats(stats);
    }

    /**
     * Records a successful API call with token usage (legacy, no provider info).
     */
    public void recordApiSuccess(int promptTokens, int candidateTokens, int totalTokens) {
        recordApiSuccess(promptTokens, candidateTokens, totalTokens, getProvider(), null, null);
    }

    /**
     * Records a failed API call with provider/model info.
     */
    public void recordApiFailure(String provider, String modelId, String modelName, String error) {
        com.najmi.oreamnos.model.UsageStats stats = getUsageStats();
        stats.recordFailure(provider, modelId, modelName, error);
        saveUsageStats(stats);
    }

    /**
     * Records a failed API call (legacy, no provider info).
     */
    public void recordApiFailure() {
        recordApiFailure(getProvider(), null, null, null);
    }

    /**
     * Resets all usage statistics.
     */
    public void resetUsageStats() {
        saveUsageStats(new com.najmi.oreamnos.model.UsageStats());
    }

    // ==================== LOG METHODS ====================

    /**
     * Logs an info message.
     */
    public void logInfo(String tag, String message) {
        com.najmi.oreamnos.model.UsageStats stats = getUsageStats();
        stats.logInfo(tag, message);
        saveUsageStats(stats);
    }

    /**
     * Logs a warning message.
     */
    public void logWarning(String tag, String message, String details) {
        com.najmi.oreamnos.model.UsageStats stats = getUsageStats();
        stats.logWarning(tag, message, details);
        saveUsageStats(stats);
    }

    /**
     * Logs an error message.
     */
    public void logError(String tag, String message, String details) {
        com.najmi.oreamnos.model.UsageStats stats = getUsageStats();
        stats.logError(tag, message, details);
        saveUsageStats(stats);
    }

    /**
     * Clears all logs.
     */
    public void clearLogs() {
        com.najmi.oreamnos.model.UsageStats stats = getUsageStats();
        stats.clearLogs();
        saveUsageStats(stats);
    }
}
