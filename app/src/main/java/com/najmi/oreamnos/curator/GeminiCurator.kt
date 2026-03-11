package com.najmi.oreamnos.curator

import com.najmi.oreamnos.services.GeminiService

/**
 * Gemini implementation of IContentCurator.
 * Wraps the existing GeminiService to conform to the curator interface.
 */
class GeminiCurator(
    apiKey: String,
    endpoint: String,
    tone: String
) : IContentCurator {

    private val geminiService = GeminiService(apiKey, endpoint, tone)

    @Throws(Exception::class)
    override suspend fun curatePost(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        tone: String?,
        length: String?
    ): String {
        return geminiService.curatePost(inputText, includeSource, keepStructure, tone, length) ?: ""
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
