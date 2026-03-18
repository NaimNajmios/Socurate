package com.najmi.oreamnos.curator

/**
 * Interface defining content curation contract.
 * Allows swapping between different AI providers (Gemini, OpenAI, Groq, etc.)
 * without changing the calling code.
 */
interface IContentCurator {

    /**
     * Curates the input text into a social media post.
     */
    @Throws(Exception::class)
    suspend fun curatePost(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String? = null
    ): String

    /**
     * Curates the input text with streaming token support.
     * Calls [onToken] for each token/segment as it arrives.
     * Returns the complete curated text when done.
     */
    @Throws(Exception::class)
    suspend fun curatePostStreaming(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String? = null,
        onToken: (String) -> Unit
    ): String

    /**
     * Sends [prompt] to the AI provider exactly as-is, with no additional system
     * prompt or post-processing. Used by [CardDataExtractor] to get raw JSON output
     * without the Malay social-media framing that [curatePost] applies.
     *
     * Implementations SHOULD override this. The default falls back to [curatePost]
     * for providers that have not yet implemented it.
     */
    @Throws(Exception::class)
    suspend fun generateRaw(prompt: String): String =
        curatePost(inputText = prompt, includeSource = false, keepStructure = true, length = null)

    /**
     * Refines an existing post based on selected refinement options.
     */
    @Throws(Exception::class)
    suspend fun refinePost(originalPost: String, refinements: List<String>, includeSource: Boolean): String

    val lastPromptTokens: Int
    val lastCandidateTokens: Int
    val lastTotalTokens: Int
}
