package com.najmi.oreamnos.curator;

import android.content.Context;

import com.najmi.oreamnos.utils.PreferencesManager;

/**
 * Factory for creating IContentCurator instances.
 * Reads user preferences to determine which AI provider to use.
 * 
 * Currently supports:
 * - Gemini (default)
 * 
 * Future extensibility:
 * - OpenAI (GPT-4, etc.)
 * - Groq (Llama, Mixtral)
 * - OpenRouter
 */
public class CuratorFactory {

    /**
     * Creates an IContentCurator based on user preferences.
     *
     * @param context Android context for accessing preferences
     * @return Configured IContentCurator instance
     */
    public static IContentCurator create(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);

        String apiKey = prefs.getApiKey();
        String endpoint = prefs.getApiEndpoint();
        String tone = prefs.getTone();

        // Currently only Gemini is supported
        // Future: Add provider selection in preferences and switch here
        // String provider = prefs.getProvider();
        // switch (provider) {
        // case "openai":
        // return new OpenAICurator(apiKey, endpoint);
        // case "groq":
        // return new GroqCurator(apiKey, endpoint);
        // default:
        // return new GeminiCurator(apiKey, endpoint, tone);
        // }

        return new GeminiCurator(apiKey, endpoint, tone);
    }

    /**
     * Creates an IContentCurator with explicit parameters.
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
}
