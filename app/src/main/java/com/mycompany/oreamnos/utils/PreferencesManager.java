package com.mycompany.oreamnos.utils;

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
    private static final String DEFAULT_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";

    public static final String TONE_FORMAL = "formal";
    public static final String TONE_CASUAL = "casual";

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
     * Clears all stored preferences.
     */
    public void clearAll() {
        securePrefs.edit().clear().apply();
    }
}
