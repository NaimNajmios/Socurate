package com.najmi.oreamnos.curator

import android.util.Log
import com.najmi.oreamnos.exceptions.RateLimitException
import kotlinx.coroutines.delay
import java.util.Random
import java.util.UUID

abstract class BaseCurator : IContentCurator {

    protected abstract val tag: String
    protected abstract val maxRetries: Int
    protected abstract val baseDelayMs: Long
    protected abstract val maxDelayMs: Long
    protected abstract val rateLimitFallbackDelayMs: Long

    protected var _lastPromptTokens: Int = 0
    protected var _lastCandidateTokens: Int = 0
    protected var _lastTotalTokens: Int = 0

    override val lastPromptTokens: Int get() = _lastPromptTokens
    override val lastCandidateTokens: Int get() = _lastCandidateTokens
    override val lastTotalTokens: Int get() = _lastTotalTokens

    protected abstract suspend fun executeRequest(prompt: String): String

    override suspend fun curatePost(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String?
    ): String {
        val startTime = System.currentTimeMillis()
        val requestId = UUID.randomUUID().toString().substring(0, 8)

        Log.i(tag, "=== CURATE POST START [$requestId] ===")

        var lastException: Exception? = null
        val rnd = Random()

        for (attempt in 1..maxRetries) {
            try {
                Log.i(tag, "[$requestId] Attempt $attempt/$maxRetries")
                val rawResponse = executeRequest(buildPrompt(inputText, includeSource, keepStructure, length))
                
                val cleanedResponse = ResponseCleanup.cleanUpResponse(rawResponse, includeSource)
                val finalText = if (includeSource) {
                    cleanedResponse
                } else {
                    ResponseCleanup.removeSourceCitation(cleanedResponse)
                }

                val totalTime = System.currentTimeMillis() - startTime
                Log.i(tag, "[$requestId] Success! Output: ${finalText.length} chars (time: ${totalTime}ms)")
                Log.i(tag, "=== CURATE POST END [$requestId] ===")

                return finalText
            } catch (rle: RateLimitException) {
                Log.w(tag, "[$requestId] Rate limit: ${rle.message}")
                throw rle
            } catch (e: Exception) {
                Log.w(tag, "[$requestId] Error on attempt $attempt: ${e.message}")
                lastException = e
                
                if (attempt < maxRetries) {
                    val delayMs = calculateRetryDelay(e, attempt, rnd, requestId)
                    Log.i(tag, "[$requestId] Sleeping ${delayMs}ms before retry")
                    delay(delayMs)
                }
            }
        }

        Log.e(tag, "[$requestId] Exhausted retries after $maxRetries attempts")
        throw lastException ?: Exception("Failed after $maxRetries retries")
    }

    override suspend fun curatePostStreaming(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String?,
        onToken: (String) -> Unit
    ): String {
        val result = curatePost(inputText, includeSource, keepStructure, length)
        
        val chunkSize = 20
        var offset = 0
        while (offset < result.length) {
            val end = minOf(offset + chunkSize, result.length)
            onToken(result.substring(offset, end))
            offset = end
            delay(30)
        }
        
        return result
    }

    override suspend fun refinePost(originalPost: String, refinements: List<String>, includeSource: Boolean): String {
        val startTime = System.currentTimeMillis()
        val requestId = UUID.randomUUID().toString().substring(0, 8)

        Log.i(tag, "=== REFINEMENT START [$requestId] ===")

        var lastException: Exception? = null
        val rnd = Random()

        for (attempt in 1..maxRetries) {
            try {
                Log.i(tag, "[$requestId] Refinement attempt $attempt/$maxRetries")
                val prompt = buildRefinementPrompt(originalPost, refinements, includeSource)
                val rawResponse = executeRequest(prompt)
                
                val cleanedResponse = ResponseCleanup.cleanUpResponse(rawResponse, includeSource)
                val finalText = if (includeSource) {
                    cleanedResponse
                } else {
                    ResponseCleanup.removeSourceCitation(cleanedResponse)
                }

                val totalTime = System.currentTimeMillis() - startTime
                Log.i(tag, "[$requestId] Refinement success! (time: ${totalTime}ms)")
                Log.i(tag, "=== REFINEMENT END [$requestId] ===")

                return finalText
            } catch (rle: RateLimitException) {
                throw rle
            } catch (e: Exception) {
                Log.w(tag, "[$requestId] Refinement error on attempt $attempt: ${e.message}")
                lastException = e
                
                if (attempt < maxRetries) {
                    val delayMs = calculateRetryDelay(e, attempt, rnd, requestId)
                    delay(delayMs)
                }
            }
        }

        throw lastException ?: Exception("Refinement failed after $maxRetries retries")
    }

    protected fun calculateRetryDelay(
        lastException: Exception?,
        attempt: Int,
        rnd: Random,
        requestId: String
    ): Long {
        return if (lastException is RateLimitException) {
            val apiDelay = lastException.retryDelayMs
            if (apiDelay > 0) {
                val delay = minOf(maxDelayMs, apiDelay)
                Log.i(tag, "[$requestId] Using API-suggested delay: ${delay}ms")
                delay
            } else {
                Log.i(tag, "[$requestId] Using fallback delay for rate limit: ${rateLimitFallbackDelayMs}ms")
                rateLimitFallbackDelayMs
            }
        } else {
            var delay = minOf(maxDelayMs, baseDelayMs * (1L shl (attempt - 1)))
            val jitter = (rnd.nextDouble() * 500L).toLong()
            delay += jitter
            delay
        }
    }

    protected abstract fun buildPrompt(inputText: String, includeSource: Boolean, keepStructure: Boolean, length: String?): String
    
    protected abstract fun buildRefinementPrompt(originalPost: String, refinements: List<String>, includeSource: Boolean): String

    protected abstract fun getProviderName(): String
}
