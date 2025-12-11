package com.najmi.oreamnos.curator;

import android.content.Context;

import com.najmi.oreamnos.utils.PreferencesManager;

/**
 * Factory for creating IContentCurator instances.
 * Reads user preferences to determine which AI provider to use.
 * 
 * Supported providers:
 * - Gemini (Google) - default
 * - Groq (Llama 3.3 70B)
 * - OpenRouter (access to free models)
 */
public class CuratorFactory {

    // Provider constants
    public static final String PROVIDER_GEMINI = "gemini";
    public static final String PROVIDER_GROQ = "groq";
    public static final String PROVIDER_OPENROUTER = "openrouter";

    // API endpoints
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // Default model IDs
    private static final String GROQ_MODEL_ID = "llama-3.3-70b-versatile";
    private static final String OPENROUTER_MODEL_ID = "google/gemini-2.0-flash-exp:free";

    /**
     * Creates an IContentCurator based on user preferences.
     *
     * @param context Android context for accessing preferences
     * @return Configured IContentCurator instance
     */
    public static IContentCurator create(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);

        String provider = prefs.getProvider();
        String tone = prefs.getTone();

        switch (provider) {
            case PROVIDER_GROQ:
                String groqKey = prefs.getGroqApiKey();
                return new OpenAICompatibleCurator(
                        groqKey,
                        GROQ_API_URL,
                        GROQ_MODEL_ID,
                        tone,
                        false // isOpenRouter = false
                );

            case PROVIDER_OPENROUTER:
                String openRouterKey = prefs.getOpenRouterApiKey();
                return new OpenAICompatibleCurator(
                        openRouterKey,
                        OPENROUTER_API_URL,
                        OPENROUTER_MODEL_ID,
                        tone,
                        true // isOpenRouter = true (requires special headers)
                );

            case PROVIDER_GEMINI:
            default:
                String geminiKey = prefs.getApiKey();
                String endpoint = prefs.getApiEndpoint();
                return new GeminiCurator(geminiKey, endpoint, tone);
        }
    }

    /**
     * Creates an IContentCurator with explicit parameters (for Gemini).
     * Useful for services that don't need PreferencesManager lookup.
     *
     * @param apiKey   API key for the provider
     * @param endpoint API endpoint URL
     * @param tone     Post tone preference
     * @return Configured IContentCurator instance
     */
    public static IContentCurator create(String apiKey, String endpoint, String tone) {
        return new GeminiCurator(apiKey, endpoint, tone);
    }

    /**
     * Gets the display name for a provider.
     */
    public static String getProviderDisplayName(String provider) {
        switch (provider) {
            case PROVIDER_GROQ:
                return "Groq (Llama 3.3)";
            case PROVIDER_OPENROUTER:
                return "OpenRouter (Free Models)";
            case PROVIDER_GEMINI:
            default:
                return "Gemini (Google)";
        }
    }
}
