package com.najmi.oreamnos.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.najmi.oreamnos.model.GenerationPill
import com.najmi.oreamnos.model.UsageStats

/**
 * Manages app preferences and secure storage for sensitive data like API keys.
 * Uses EncryptedSharedPreferences for secure storage of API credentials.
 */
class PreferencesManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val securePrefs: SharedPreferences = getEncryptedPreferences()

    /**
     * Gets or creates encrypted shared preferences.
     * Falls back to regular SharedPreferences if encryption is not available.
     */
    private fun getEncryptedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                appContext,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create encrypted preferences, using regular preferences", e)
            appContext.getSharedPreferences("${PREFS_FILE_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    // ==================== GEMINI API KEY ====================

    fun saveApiKey(apiKey: String): Boolean {
        return try {
            securePrefs.edit().putString(KEY_API_KEY, apiKey).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save API key", e)
            false
        }
    }

    fun getApiKey(): String? {
        return try {
            securePrefs.getString(KEY_API_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve API key", e)
            null
        }
    }

    fun hasApiKey(): Boolean {
        val key = getApiKey()
        return !key.isNullOrBlank()
    }

    // ==================== API ENDPOINT ====================

    fun saveApiEndpoint(endpoint: String) {
        securePrefs.edit().putString(KEY_API_ENDPOINT, endpoint).apply()
    }

    fun getApiEndpoint(): String {
        return securePrefs.getString(KEY_API_ENDPOINT, DEFAULT_ENDPOINT) ?: DEFAULT_ENDPOINT
    }

    fun resetApiEndpointToDefault() {
        saveApiEndpoint(DEFAULT_ENDPOINT)
    }

    // ==================== HASHTAGS ====================

    fun saveHashtags(hashtags: String) {
        securePrefs.edit().putString(KEY_HASHTAGS, hashtags).apply()
    }

    fun getHashtags(): String {
        return securePrefs.getString(KEY_HASHTAGS, DEFAULT_HASHTAGS) ?: DEFAULT_HASHTAGS
    }

    fun setHashtagsEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(KEY_HASHTAGS_ENABLED, enabled).apply()
    }

    fun areHashtagsEnabled(): Boolean {
        return securePrefs.getBoolean(KEY_HASHTAGS_ENABLED, true)
    }

    fun getFormattedHashtags(): String {
        val hashtags = getHashtags()
        if (hashtags.isBlank()) return ""

        return hashtags.split(Regex("[,\\s]+"))
            .filter { it.isNotBlank() }
            .joinToString("\n") { tag ->
                if (tag.startsWith("#")) tag else "#$tag"
            }
    }

    // ==================== THEME ====================

    fun saveTheme(theme: String) {
        securePrefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getTheme(): String {
        return securePrefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    // ==================== SOURCE CITATION ====================

    fun saveSourceEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(KEY_SOURCE_ENABLED, enabled).apply()
    }

    fun isSourceEnabled(): Boolean {
        return securePrefs.getBoolean(KEY_SOURCE_ENABLED, true)
    }

    // ==================== VISION AI PREFERENCE ====================

    fun saveVisionModelPreference(modelId: String) {
        securePrefs.edit().putString(KEY_VISION_MODEL_PREFERENCE, modelId).apply()
    }

    fun getVisionModelPreference(): String {
        return securePrefs.getString(KEY_VISION_MODEL_PREFERENCE, "auto") ?: "auto"
    }

    // ==================== WATERMARK ====================
    
    fun saveWatermarkPath(path: String?) {
        securePrefs.edit().putString(KEY_SAVED_WATERMARK_PATH, path).apply()
    }

    fun getWatermarkPath(): String? {
        return securePrefs.getString(KEY_SAVED_WATERMARK_PATH, null)
    }

    // ==================== AI PROVIDER ====================

    fun saveProvider(provider: String) {
        securePrefs.edit().putString(KEY_PROVIDER, provider).apply()
    }

    fun getProvider(): String {
        return securePrefs.getString(KEY_PROVIDER, PROVIDER_GEMINI) ?: PROVIDER_GEMINI
    }

    // ==================== PROVIDER API KEYS ====================

    fun saveGroqApiKey(apiKey: String): Boolean {
        return try {
            securePrefs.edit().putString(KEY_GROQ_API_KEY, apiKey).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save Groq API key", e)
            false
        }
    }

    fun getGroqApiKey(): String? {
        return try {
            securePrefs.getString(KEY_GROQ_API_KEY, null)
        } catch (e: Exception) {
            null
        }
    }

    fun saveOpenRouterApiKey(apiKey: String): Boolean {
        return try {
            securePrefs.edit().putString(KEY_OPENROUTER_API_KEY, apiKey).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save OpenRouter API key", e)
            false
        }
    }

    fun getOpenRouterApiKey(): String? {
        return try {
            securePrefs.getString(KEY_OPENROUTER_API_KEY, null)
        } catch (e: Exception) {
            null
        }
    }

    fun saveCerebrasApiKey(apiKey: String): Boolean {
        return try {
            securePrefs.edit().putString(KEY_CEREBRAS_API_KEY, apiKey).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save Cerebras API key", e)
            false
        }
    }

    fun getCerebrasApiKey(): String? {
        return try {
            securePrefs.getString(KEY_CEREBRAS_API_KEY, null)
        } catch (e: Exception) {
            null
        }
    }

    fun hasApiKeyForCurrentProvider(): Boolean {
        return when (getProvider()) {
            PROVIDER_GROQ -> !getGroqApiKey().isNullOrBlank()
            PROVIDER_OPENROUTER -> !getOpenRouterApiKey().isNullOrBlank()
            PROVIDER_CEREBRAS -> !getCerebrasApiKey().isNullOrBlank()
            else -> hasApiKey()
        }
    }
    
    /**
     * Checks if a specific provider has an API key configured.
     */
    fun hasApiKeyForProvider(provider: String): Boolean {
        return when (provider) {
            PROVIDER_GROQ -> !getGroqApiKey().isNullOrBlank()
            PROVIDER_OPENROUTER -> !getOpenRouterApiKey().isNullOrBlank()
            PROVIDER_CEREBRAS -> !getCerebrasApiKey().isNullOrBlank()
            PROVIDER_GEMINI -> hasApiKey()
            else -> false
        }
    }
    
    /**
     * Gets the recommended fallback provider based on the current provider.
     * Follows the fallback chain: Gemini -> Groq -> OpenRouter -> Cerebras -> Gemini
     */
    fun getRecommendedFallbackProvider(currentProvider: String): String {
        return when (currentProvider) {
            PROVIDER_GEMINI -> PROVIDER_GROQ
            PROVIDER_GROQ -> PROVIDER_OPENROUTER
            PROVIDER_OPENROUTER -> PROVIDER_CEREBRAS
            PROVIDER_CEREBRAS -> PROVIDER_GEMINI
            else -> PROVIDER_GROQ
        }
    }
    
    /**
     * Gets all providers with their configuration status.
     * Returns a list of Triple(providerValue, displayName, hasApiKey)
     */
    fun getAllProvidersWithStatus(): List<Triple<String, String, Boolean>> {
        return listOf(
            Triple(PROVIDER_GEMINI, "Gemini (Google)", hasApiKey()),
            Triple(PROVIDER_GROQ, "Groq (Llama 3.3)", !getGroqApiKey().isNullOrBlank()),
            Triple(PROVIDER_OPENROUTER, "OpenRouter (Free Models)", !getOpenRouterApiKey().isNullOrBlank()),
            Triple(PROVIDER_CEREBRAS, "Cerebras (Fast Inference)", !getCerebrasApiKey().isNullOrBlank())
        )
    }


    // ==================== MODELS PER PROVIDER ====================

    fun saveModelForProvider(provider: String, modelId: String) {
        val key = when (provider) {
            PROVIDER_GROQ -> KEY_GROQ_MODEL
            PROVIDER_OPENROUTER -> KEY_OPENROUTER_MODEL
            PROVIDER_CEREBRAS -> KEY_CEREBRAS_MODEL
            else -> KEY_GEMINI_MODEL
        }
        securePrefs.edit().putString(key, modelId).apply()
    }

    fun getModelForProvider(provider: String): String {
        val (key, defaultModel) = when (provider) {
            PROVIDER_GROQ -> KEY_GROQ_MODEL to DEFAULT_GROQ_MODEL
            PROVIDER_OPENROUTER -> KEY_OPENROUTER_MODEL to DEFAULT_OPENROUTER_MODEL
            PROVIDER_CEREBRAS -> KEY_CEREBRAS_MODEL to DEFAULT_CEREBRAS_MODEL
            else -> KEY_GEMINI_MODEL to DEFAULT_GEMINI_MODEL
        }
        return securePrefs.getString(key, defaultModel) ?: defaultModel
    }

    // ==================== CUSTOM REFINEMENT PILLS ====================

    fun savePills(pills: List<GenerationPill>) {
        val json = GenerationPill.toJson(pills)
        securePrefs.edit().putString(KEY_PILLS, json).apply()
    }

    fun getPills(): List<GenerationPill> {
        val json = securePrefs.getString(KEY_PILLS, null)
        return GenerationPill.fromJson(json)
    }

    fun savePill(pill: GenerationPill) {
        val pills = getPills().toMutableList()
        val index = pills.indexOfFirst { it.id == pill.id }
        if (index >= 0) {
            pills[index] = pill
        } else {
            pills.add(pill)
        }
        savePills(pills)
    }

    fun deletePill(pillId: String) {
        val pills = getPills().filter { it.id != pillId }
        savePills(pills)
    }

    // ==================== USAGE STATS ====================

    fun getUsageStats(): UsageStats {
        val json = securePrefs.getString(KEY_USAGE_STATS, null)
        return UsageStats.fromJson(json)
    }

    fun saveUsageStats(stats: UsageStats) {
        securePrefs.edit().putString(KEY_USAGE_STATS, stats.toJson()).apply()
    }

    @JvmOverloads
    fun recordApiSuccess(
        promptTokens: Int,
        candidateTokens: Int,
        totalTokens: Int,
        durationMs: Long = 0,
        provider: String? = getProvider(),
        modelId: String? = null,
        modelName: String? = null
    ) {
        val stats = getUsageStats()
        stats.recordSuccess(promptTokens, candidateTokens, totalTokens, provider, modelId, modelName, durationMs)
        saveUsageStats(stats)
    }

    @JvmOverloads
    fun recordApiFailure(
        durationMs: Long = 0,
        provider: String? = getProvider(),
        modelId: String? = null,
        modelName: String? = null,
        error: String? = null
    ) {
        val stats = getUsageStats()
        stats.recordFailure(provider, modelId, modelName, error, durationMs)
        saveUsageStats(stats)
    }

    fun resetUsageStats() {
        saveUsageStats(UsageStats())
    }

    // ==================== LOG METHODS ====================

    fun logInfo(tag: String, message: String) {
        val stats = getUsageStats()
        stats.logInfo(tag, message)
        saveUsageStats(stats)
    }

    fun logWarning(tag: String, message: String, details: String?) {
        val stats = getUsageStats()
        stats.logWarning(tag, message, details)
        saveUsageStats(stats)
    }

    fun logError(tag: String, message: String, details: String?) {
        val stats = getUsageStats()
        stats.logError(tag, message, details)
        saveUsageStats(stats)
    }

    fun clearLogs() {
        val stats = getUsageStats()
        stats.clearLogs()
        saveUsageStats(stats)
    }

    fun clearAll() {
        securePrefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "PreferencesManager"
        private const val PREFS_FILE_NAME = "oreamnos_secure_prefs"

        // Keys
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_API_ENDPOINT = "api_endpoint"
        private const val KEY_HASHTAGS = "default_hashtags"
        private const val KEY_HASHTAGS_ENABLED = "hashtags_enabled"
        private const val KEY_SOURCE_ENABLED = "source_enabled"
        private const val KEY_THEME = "app_theme"
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_GROQ_API_KEY = "groq_api_key"
        private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
        private const val KEY_CEREBRAS_API_KEY = "cerebras_api_key"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_GROQ_MODEL = "groq_model"
        private const val KEY_OPENROUTER_MODEL = "openrouter_model"
        private const val KEY_CEREBRAS_MODEL = "cerebras_model"
        private const val KEY_PILLS = "generation_pills"
        private const val KEY_USAGE_STATS = "usage_stats"
        private const val KEY_TEXT_SIZE = "output_text_size"
        private const val KEY_SAVED_WATERMARK_PATH = "saved_watermark_path"
        private const val KEY_VISION_MODEL_PREFERENCE = "vision_model_preference"

        // Defaults
        private const val DEFAULT_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
        private const val DEFAULT_HASHTAGS = "#BolaSepak #Football"
        private const val DEFAULT_GEMINI_MODEL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"
        private const val DEFAULT_GROQ_MODEL = "llama-3.3-70b-versatile"
        private const val DEFAULT_OPENROUTER_MODEL = "meta-llama/llama-3.3-70b-instruct:free"
        private const val DEFAULT_CEREBRAS_MODEL = "llama-3.3-70b"


        // Theme constants
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_DEEP_BLUE = "deep_blue"
        const val THEME_MIDNIGHT = "midnight"
        const val THEME_SOLARIZED = "solarized"
        const val THEME_CYBERPUNK = "cyberpunk"
        const val THEME_MATCHDAY = "matchday"

        // Provider constants
        const val PROVIDER_GEMINI = "gemini"
        const val PROVIDER_GROQ = "groq"
        const val PROVIDER_OPENROUTER = "openrouter"
        const val PROVIDER_CEREBRAS = "cerebras"
        
        // Text size constants
        const val TEXT_SIZE_SMALL = 11
        const val TEXT_SIZE_MEDIUM = 13
        const val TEXT_SIZE_LARGE = 15
        const val TEXT_SIZE_EXTRA_LARGE = 17
    }
    
    // ==================== TEXT SIZE PREFERENCE ====================
    
    fun saveTextSize(size: Int) {
        securePrefs.edit().putInt(KEY_TEXT_SIZE, size).apply()
    }
    
    fun getTextSize(): Int {
        return securePrefs.getInt(KEY_TEXT_SIZE, TEXT_SIZE_MEDIUM)
    }
}
