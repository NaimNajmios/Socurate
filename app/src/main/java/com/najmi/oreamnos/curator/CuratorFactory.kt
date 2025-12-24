package com.najmi.oreamnos.curator

import android.content.Context
import com.najmi.oreamnos.utils.PreferencesManager

/**
 * Factory for creating IContentCurator instances.
 * Reads user preferences to determine which AI provider to use.
 *
 * Supported providers:
 * - Gemini (Google) - default
 * - Groq (Llama 3.3 70B)
 * - OpenRouter (access to free models)
 * - Cerebras (ultra-fast inference)
 */
object CuratorFactory {

    // Provider constants
    const val PROVIDER_GEMINI = "gemini"
    const val PROVIDER_GROQ = "groq"
    const val PROVIDER_OPENROUTER = "openrouter"
    const val PROVIDER_CEREBRAS = "cerebras"

    // API endpoints
    private const val GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"
    private const val CEREBRAS_API_URL = "https://api.cerebras.ai/v1/chat/completions"

    // Default model IDs
    private const val GROQ_MODEL_ID = "llama-3.3-70b-versatile"
    private const val OPENROUTER_MODEL_ID = "meta-llama/llama-3.3-70b-instruct:free"
    private const val CEREBRAS_MODEL_ID = "llama-3.3-70b"

    /**
     * Creates an IContentCurator based on user preferences.
     *
     * @param context Android context for accessing preferences
     * @return Configured IContentCurator instance
     */
    @JvmStatic
    fun create(context: Context): IContentCurator {
        val prefs = PreferencesManager(context)
        val provider = prefs.getProvider()
        val tone = prefs.getTone()

        return when (provider) {
            PROVIDER_GROQ -> {
                val groqKey = prefs.getGroqApiKey()
                OpenAICompatibleCurator(
                    apiKey = groqKey.orEmpty(),
                    baseUrl = GROQ_API_URL,
                    modelId = GROQ_MODEL_ID,
                    tone = tone,
                    isOpenRouter = false
                )
            }
            PROVIDER_OPENROUTER -> {
                val openRouterKey = prefs.getOpenRouterApiKey()
                OpenAICompatibleCurator(
                    apiKey = openRouterKey.orEmpty(),
                    baseUrl = OPENROUTER_API_URL,
                    modelId = OPENROUTER_MODEL_ID,
                    tone = tone,
                    isOpenRouter = true
                )
            }
            PROVIDER_CEREBRAS -> {
                val cerebrasKey = prefs.getCerebrasApiKey()
                OpenAICompatibleCurator(
                    apiKey = cerebrasKey.orEmpty(),
                    baseUrl = CEREBRAS_API_URL,
                    modelId = CEREBRAS_MODEL_ID,
                    tone = tone,
                    isOpenRouter = false
                )
            }
            else -> {
                val geminiKey = prefs.getApiKey()
                val endpoint = prefs.getApiEndpoint()
                GeminiCurator(geminiKey.orEmpty(), endpoint.orEmpty(), tone)
            }
        }
    }

    /**
     * Creates an IContentCurator with explicit parameters (for Gemini).
     * Useful for services that don't need PreferencesManager lookup.
     */
    @JvmStatic
    fun create(apiKey: String, endpoint: String, tone: String): IContentCurator {
        return GeminiCurator(apiKey, endpoint, tone)
    }

    /**
     * Gets the display name for a provider.
     */
    @JvmStatic
    fun getProviderDisplayName(provider: String): String {
        return when (provider) {
            PROVIDER_GROQ -> "Groq (Llama 3.3)"
            PROVIDER_OPENROUTER -> "OpenRouter (Free Models)"
            PROVIDER_CEREBRAS -> "Cerebras (Fast Inference)"
            else -> "Gemini (Google)"
        }
    }
}
