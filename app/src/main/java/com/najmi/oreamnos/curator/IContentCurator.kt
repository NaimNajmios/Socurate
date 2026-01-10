package com.najmi.oreamnos.curator

/**
 * Interface defining content curation contract.
 * Allows swapping between different AI providers (Gemini, OpenAI, Groq, etc.)
 * without changing the calling code.
 */
interface IContentCurator {

    /**
     * Curates the input text into a social media post.
     *
     * @param inputText     The text to curate
     * @param includeSource Whether to include source citation
     * @param keepStructure Whether to preserve original formatting/structure
     * @return The curated post
     * @throws Exception if curation fails
     */
    @Throws(Exception::class)
    suspend fun curatePost(inputText: String, includeSource: Boolean, keepStructure: Boolean): String

    /**
     * Refines an existing post based on selected refinement options.
     *
     * @param originalPost  The post to refine
     * @param refinements   List of refinement options (e.g., "rephrase", "formal")
     * @param includeSource Whether to include source citation
     * @return The refined post
     * @throws Exception if refinement fails
     */
    @Throws(Exception::class)
    suspend fun refinePost(originalPost: String, refinements: List<String>, includeSource: Boolean): String

    /** Gets the last prompt token count from API response. */
    val lastPromptTokens: Int

    /** Gets the last candidate (response) token count from API response. */
    val lastCandidateTokens: Int

    /** Gets the last total token count from API response. */
    val lastTotalTokens: Int
}
