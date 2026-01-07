package com.najmi.oreamnos.curator

import android.util.Log
import com.najmi.oreamnos.exceptions.RateLimitException
import com.najmi.oreamnos.prompts.PromptManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * OpenAI-compatible API curator that works with Groq, OpenRouter, and Cerebras.
 * These providers use the same OpenAI chat completions format.
 *
 * Supported providers:
 * - Groq: Fast inference with Llama 3.3
 * - OpenRouter: Access to multiple free models
 * - Cerebras: Ultra-fast inference with various models
 */
class OpenAICompatibleCurator(
    private val apiKey: String,
    private val baseUrl: String,
    private val modelId: String,
    private val tone: String,
    private val isOpenRouter: Boolean
) : IContentCurator {

    companion object {
        private const val TAG = "OpenAICompatibleCurator"
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        
        // Pre-compiled Regex Patterns for response cleanup
        private val HORIZONTAL_RULE_PATTERN: Pattern = Pattern.compile("(?m)^-{3,}\\s*$")
        private val ASTERISK_TEXT_PATTERN: Pattern = Pattern.compile("\\*+(.*?)\\*+")
        private val MULTIPLE_NEWLINES_PATTERN: Pattern = Pattern.compile("\\n\\s*\\n\\s*\\n+")
        private val HORIZONTAL_WHITESPACE_PATTERN: Pattern = Pattern.compile("[ \\t]+")
        private val SOURCE_CITATION_PATTERN: Pattern = Pattern.compile("(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$")
        private val TRAILING_NEWLINES_PATTERN: Pattern = Pattern.compile("\\n+$")
        private val BULLET_POINT_PATTERN: Pattern = Pattern.compile("(?m)^(\\s*)[-*>\u2022\u25e6\u25aa\u25ab\u2023\u2043](\\s+)")
    }

    private val promptManager = PromptManager()

    // Token counts from last API call
    private var _lastPromptTokens = 0
    private var _lastCandidateTokens = 0
    private var _lastTotalTokens = 0

    override val lastPromptTokens: Int get() = _lastPromptTokens
    override val lastCandidateTokens: Int get() = _lastCandidateTokens
    override val lastTotalTokens: Int get() = _lastTotalTokens

    @Throws(Exception::class)
    override fun curatePost(inputText: String, includeSource: Boolean, keepStructure: Boolean): String {
        val systemPrompt = "You are a professional social media content writer for a Malaysian football club. " +
                "Write in Malaysian Malay (Bahasa Malaysia) only. Do not include hashtags. Do not include emojis."
        val userPrompt = promptManager.buildInitialPrompt(tone, inputText, includeSource, keepStructure)
        val rawResult = callApi(systemPrompt, userPrompt)
        return cleanUpResponse(rawResult, includeSource)
    }

    @Throws(Exception::class)
    override fun refinePost(originalPost: String, refinements: List<String>, includeSource: Boolean): String {
        val systemPrompt = "You are refining a Malaysian Malay social media post about football. " +
                "Apply improvements while maintaining Bahasa Malaysia. Do not include hashtags. Do not include emojis."
        val userPrompt = promptManager.buildRefinementPrompt(originalPost, refinements, includeSource)
        val rawResult = callApi(systemPrompt, userPrompt)
        return cleanUpResponse(rawResult, includeSource)
    }

    /**
     * Makes the API call with retry logic.
     */
    @Throws(Exception::class)
    private fun callApi(systemPrompt: String, userPrompt: String): String {
        var retryCount = 0
        var delayMs = INITIAL_RETRY_DELAY_MS
        var lastException: Exception? = null

        while (retryCount < MAX_RETRIES) {
            try {
                return executeRequest(systemPrompt, userPrompt)
            } catch (rle: RateLimitException) {
                // Rate limit exceptions should be thrown immediately for fallback handling
                throw rle
            } catch (e: Exception) {
                lastException = e
                retryCount++
                Log.w(TAG, "API call failed (attempt $retryCount/$MAX_RETRIES): ${e.message}")

                if (retryCount >= MAX_RETRIES) {
                    throw e
                }

                // Exponential backoff
                Thread.sleep(delayMs)
                delayMs *= 2
            }
        }

        throw lastException ?: Exception("Max retries exceeded")
    }

    /**
     * Executes the HTTP request to the OpenAI-compatible API.
     */
    @Throws(Exception::class)
    private fun executeRequest(systemPrompt: String, userPrompt: String): String {
        val url = URL(baseUrl)
        val conn = url.openConnection() as HttpURLConnection

        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")

            // OpenRouter requires additional headers
            if (isOpenRouter) {
                conn.setRequestProperty("HTTP-Referer", "https://github.com/socurate-app")
                conn.setRequestProperty("X-Title", "Socurate Football Content Curator")
            }

            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 60000

            // Build request body in OpenAI format
            val requestBody = buildRequestBody(systemPrompt, userPrompt)

            Log.d(TAG, "Sending request to: $baseUrl")
            Log.d(TAG, "Model: $modelId")

            // Write request
            conn.outputStream.use { os ->
                val input = requestBody.toString().toByteArray(StandardCharsets.UTF_8)
                os.write(input)
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "Response code: $responseCode")

            return if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                val response = readStream(conn.inputStream)
                parseResponse(response)
            } else {
                // Read error response safely (errorStream can be null)
                val errorResponse = readStream(conn.errorStream)

                Log.e(TAG, "API Error: $errorResponse")

                // Check for rate limit (429)
                if (responseCode == 429) {
                    val providerName = if (isOpenRouter) "openrouter" else "groq"
                    throw RateLimitException(
                        "Rate limit exceeded for $providerName",
                        0, // OpenAI-compatible APIs don't always provide retry delay
                        providerName
                    )
                }

                throw Exception("API error ($responseCode): ${parseErrorMessage(errorResponse.toString())}")
            }
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Safely reads an input stream into a String.
     * Returns empty string if stream is null.
     */
    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        val response = StringBuilder()
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                response.append(line)
            }
        }
        return response.toString()
    }

    /**
     * Builds the request body in OpenAI chat completions format.
     */
    @Throws(Exception::class)
    private fun buildRequestBody(systemPrompt: String, userPrompt: String): JSONObject {
        val body = JSONObject()
        body.put("model", modelId)

        val messages = JSONArray()

        // System message
        val systemMessage = JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        }
        messages.put(systemMessage)

        // User message
        val userMessage = JSONObject().apply {
            put("role", "user")
            put("content", userPrompt)
        }
        messages.put(userMessage)

        body.put("messages", messages)
        body.put("temperature", 0.7)
        body.put("max_tokens", 2048)

        return body
    }

    /**
     * Parses the OpenAI-format response.
     * Format: {"choices": [{"message": {"content": "..."}}], "usage": {...}}
     */
    @Throws(Exception::class)
    private fun parseResponse(responseJson: String): String {
        val response = JSONObject(responseJson)

        // Extract token usage
        if (response.has("usage")) {
            val usage = response.getJSONObject("usage")
            _lastPromptTokens = usage.optInt("prompt_tokens", 0)
            _lastCandidateTokens = usage.optInt("completion_tokens", 0)
            _lastTotalTokens = usage.optInt("total_tokens", 0)
            Log.d(TAG, "Token usage - Prompt: $_lastPromptTokens, Completion: $_lastCandidateTokens, Total: $_lastTotalTokens")
        }

        // Extract content
        val choices = response.getJSONArray("choices")
        if (choices.length() == 0) {
            throw Exception("No choices in response")
        }

        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        val content = message.getString("content")

        return content.trim()
    }

    /**
     * Cleans up the AI response by removing unwanted formatting and content.
     */
    private fun cleanUpResponse(response: String, includeSource: Boolean): String {
        if (response.isBlank()) return response

        var cleaned = response.trim()

        // Remove horizontal rule markers
        cleaned = HORIZONTAL_RULE_PATTERN.matcher(cleaned).replaceAll("")

        // Remove markdown asterisks while preserving content
        cleaned = ASTERISK_TEXT_PATTERN.matcher(cleaned).replaceAll("$1")

        // Normalize bullet points to use • character
        cleaned = BULLET_POINT_PATTERN.matcher(cleaned).replaceAll("$1•$2")

        // Clean up spacing
        cleaned = MULTIPLE_NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n")
        cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ")
        cleaned = cleaned.trim()

        // Remove source citation if not requested
        if (!includeSource) {
            cleaned = SOURCE_CITATION_PATTERN.matcher(cleaned).replaceAll("")
            cleaned = TRAILING_NEWLINES_PATTERN.matcher(cleaned).replaceAll("").trim()
        }

        // If too short after cleaning, return original
        if (cleaned.length < 50) {
            Log.w(TAG, "Response too short after cleaning, returning original")
            return response
        }

        return cleaned
    }

    /**
     * Parses error message from API error response.
     */
    private fun parseErrorMessage(errorJson: String): String {
        return try {
            val error = JSONObject(errorJson)
            if (error.has("error")) {
                val errorObj = error.getJSONObject("error")
                errorObj.optString("message", "Unknown error")
            } else {
                errorJson
            }
        } catch (e: Exception) {
            errorJson
        }
    }
}
