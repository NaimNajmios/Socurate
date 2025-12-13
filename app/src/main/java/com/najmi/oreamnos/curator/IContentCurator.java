package com.najmi.oreamnos.curator;

import java.util.List;

/**
 * Interface defining content curation contract.
 * Allows swapping between different AI providers (Gemini, OpenAI, Groq, etc.)
 * without changing the calling code.
 */
public interface IContentCurator {

    /**
     * Curates the input text into a social media post.
     *
     * @param inputText     The text to curate
     * @param includeSource Whether to include source citation
     * @param keepStructure Whether to preserve original formatting/structure
     * @return The curated post
     * @throws Exception if curation fails
     */
    String curatePost(String inputText, boolean includeSource, boolean keepStructure) throws Exception;

    /**
     * Refines an existing post based on selected refinement options.
     *
     * @param originalPost  The post to refine
     * @param refinements   List of refinement options (e.g., "rephrase", "formal")
     * @param includeSource Whether to include source citation
     * @return The refined post
     * @throws Exception if refinement fails
     */
    String refinePost(String originalPost, List<String> refinements, boolean includeSource) throws Exception;

    /**
     * Gets the last prompt token count from API response.
     */
    int getLastPromptTokens();

    /**
     * Gets the last candidate (response) token count from API response.
     */
    int getLastCandidateTokens();

    /**
     * Gets the last total token count from API response.
     */
    int getLastTotalTokens();
}
