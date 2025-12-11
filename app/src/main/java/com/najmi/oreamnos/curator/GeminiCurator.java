package com.najmi.oreamnos.curator;

import com.najmi.oreamnos.services.GeminiService;

import java.util.List;

/**
 * Gemini implementation of IContentCurator.
 * Wraps the existing GeminiService to conform to the curator interface.
 */
public class GeminiCurator implements IContentCurator {

    private final GeminiService geminiService;

    /**
     * Creates a new GeminiCurator.
     *
     * @param apiKey   Gemini API key
     * @param endpoint API endpoint URL
     * @param tone     Post tone ("formal" or "casual")
     */
    public GeminiCurator(String apiKey, String endpoint, String tone) {
        this.geminiService = new GeminiService(apiKey, endpoint, tone);
    }

    @Override
    public String curatePost(String inputText, boolean includeSource) throws Exception {
        return geminiService.curatePost(inputText, includeSource);
    }

    @Override
    public String refinePost(String originalPost, List<String> refinements, boolean includeSource) throws Exception {
        return geminiService.refinePost(originalPost, refinements, includeSource);
    }

    @Override
    public int getLastPromptTokens() {
        return geminiService.getLastPromptTokens();
    }

    @Override
    public int getLastCandidateTokens() {
        return geminiService.getLastCandidateTokens();
    }

    @Override
    public int getLastTotalTokens() {
        return geminiService.getLastTotalTokens();
    }
}
