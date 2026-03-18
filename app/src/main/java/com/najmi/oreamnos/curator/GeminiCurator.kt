package com.najmi.oreamnos.curator

import com.najmi.oreamnos.services.GeminiService

/**
 * Gemini implementation of IContentCurator.
 * Wraps the existing GeminiService to conform to the curator interface.
 */
class GeminiCurator(
    apiKey: String,
    endpoint: String
) : IContentCurator {

    private val geminiService = GeminiService(apiKey, endpoint)

    @Throws(Exception::class)
    override suspend fun curatePost(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String?
    ): String {
        return geminiService.curatePost(inputText, includeSource, keepStructure, length) ?: ""
    }

    @Throws(Exception::class)
    override suspend fun curatePostStreaming(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String?,
        onToken: (String) -> Unit
    ): String {
        return geminiService.curatePostStreaming(inputText, includeSource, keepStructure, length, onToken) ?: ""
    }

    @Throws(Exception::class)
    override suspend fun refinePost(originalPost: String, refinements: List<String>, includeSource: Boolean): String {
        return geminiService.refinePost(originalPost, refinements, includeSource) ?: ""
    }

    override val lastPromptTokens: Int
        get() = geminiService.lastPromptTokens

    override val lastCandidateTokens: Int
        get() = geminiService.lastCandidateTokens

    override val lastTotalTokens: Int
        get() = geminiService.lastTotalTokens
}
