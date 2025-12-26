package com.najmi.oreamnos.services

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.najmi.oreamnos.curator.IContentCurator
import com.najmi.oreamnos.exceptions.RateLimitException
import com.najmi.oreamnos.prompts.PromptManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Handles communication with the Google Gemini API for content curation.
 * Ported from the original web application with Android-specific optimizations.
 * Features retry mechanism with exponential backoff and response cleaning.
 */
class GeminiService(
    private val apiKey: String,
    private val endpoint: String,
    private val tone: String = "formal"
) : IContentCurator {

    private val gson = Gson()

    // Last request usage metadata
    private var _lastPromptTokens: Int = 0
    private var _lastCandidateTokens: Int = 0
    private var _lastTotalTokens: Int = 0

    override val lastPromptTokens: Int get() = _lastPromptTokens
    override val lastCandidateTokens: Int get() = _lastCandidateTokens
    override val lastTotalTokens: Int get() = _lastTotalTokens

    /**
     * Curates the input text into a football social media post in Malaysian Malay.
     * Implements retry logic with exponential backoff for transient errors.
     */
    @Throws(Exception::class)
    override fun curatePost(inputText: String, includeSource: Boolean, keepStructure: Boolean): String {
        val startTime = System.currentTimeMillis()
        val requestId = UUID.randomUUID().toString().substring(0, 8)

        Log.i(TAG, "=== GEMINI API CALL START [$requestId] ===")
        Log.i(TAG, "[$requestId] Input text length: ${inputText.length}")
        Log.i(TAG, "[$requestId] Include source: $includeSource")
        Log.i(TAG, "[$requestId] Keep structure: $keepStructure")

        // Validate inputs
        if (apiKey.isBlank()) throw Exception("Invalid or missing Gemini API key")
        if (endpoint.isBlank()) throw Exception("Invalid Gemini API endpoint")
        if (inputText.isBlank()) throw Exception("Input text is required")

        // Build the prompt based on tone
        val prompt = PromptManager().buildInitialPrompt(tone, inputText, includeSource, keepStructure)

        // Build request JSON
        val requestJson = buildRequestJson(prompt)
        val requestBodyString = gson.toJson(requestJson)
        Log.d(TAG, "[$requestId] Request body length: ${requestBodyString.length}")

        // Retry loop
        var rawResult: String? = null
        var lastException: Exception? = null
        val rnd = Random()

        for (attempt in 1..MAX_RETRIES) {
            try {
                Log.i(TAG, "[$requestId] Gemini attempt $attempt/$MAX_RETRIES")

                val urlWithKey = "$endpoint?key=$apiKey"
                val body = requestBodyString.toRequestBody(JSON)
                val request = Request.Builder()
                    .url(urlWithKey)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val connectionStart = System.currentTimeMillis()
                val response = sharedClient.newCall(request).execute()
                val connectionEnd = System.currentTimeMillis()

                val code = response.code
                Log.i(TAG, "[$requestId] Response code: $code (time: ${connectionEnd - connectionStart}ms) on attempt $attempt")

                if (code >= 400) {
                    val errorBody = response.body?.string() ?: ""
                    response.close()

                    // Check if transient error (retry)
                    if (code == 503 || code == 429 || code in 500..599) {
                        val errorType = if (code == 429) "Rate limit (quota)" else "Server error"
                        Log.w(TAG, "[$requestId] $errorType $code - will retry (attempt $attempt)")

                        // For 429, parse retry delay from API response
                        var apiSuggestedDelay: Long = 0
                        if (code == 429) {
                            apiSuggestedDelay = parseRetryDelay(errorBody, requestId)
                            if (apiSuggestedDelay > 0) {
                                Log.i(TAG, "[$requestId] API requests wait of ${apiSuggestedDelay}ms")
                            } else {
                                Log.w(TAG, "[$requestId] Could not parse retry delay, using default backoff")
                            }
                        }

                        lastException = RateLimitException(
                            "Gemini ${errorType.lowercase()}: $code. $errorBody",
                            apiSuggestedDelay,
                            "gemini"
                        )
                    } else {
                        // Permanent error
                        Log.e(TAG, "[$requestId] Permanent error: $code - $errorBody")
                        throw Exception("Gemini API error: $code. $errorBody")
                    }
                } else {
                    // Success
                    rawResult = response.body?.string()
                    response.close()
                    Log.d(TAG, "[$requestId] Raw response length: ${rawResult?.length ?: 0}")
                    lastException = null
                    break
                }
            } catch (ioe: IOException) {
                Log.w(TAG, "[$requestId] Network error on attempt $attempt: ${ioe.message}")
                lastException = ioe
            } catch (e: Exception) {
                Log.e(TAG, "[$requestId] Error on attempt $attempt: ${e.message}")
                lastException = e
                // If it's not a transient error, don't retry
                if (e !is IOException) throw e
            }

            // Exponential backoff if not last attempt
            if (attempt < MAX_RETRIES) {
                val delay = calculateRetryDelay(lastException, attempt, rnd, requestId)
                Log.i(TAG, "[$requestId] Sleeping ${delay}ms before retry")
                Thread.sleep(delay)
            }
        }

        // Check if we got a result
        if (rawResult == null) {
            val totalTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "[$requestId] API exhausted retries after ${totalTime}ms")

            if (lastException != null) {
                if (lastException is RateLimitException) {
                    throw lastException
                }
                throw Exception("Gemini API failed after retries: ${lastException.message}", lastException)
            } else {
                throw Exception("Gemini API returned no result after retries")
            }
        }

        // Parse response
        return try {
            val root = gson.fromJson(rawResult, JsonObject::class.java)
            var curatedText = extractTextFromJson(root)

            if (curatedText.isNullOrBlank()) {
                Log.w(TAG, "[$requestId] Extracted text is empty")
                curatedText = "Gagal mendapatkan hasil dari Gemini."
            } else {
                curatedText = cleanUpResponse(curatedText)
                if (!includeSource) {
                    curatedText = removeSourceCitation(curatedText)
                }
            }

            extractUsageMetadata(root)

            val totalTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "[$requestId] Success! Output: ${curatedText.length} chars (total time: ${totalTime}ms)")
            Log.i(TAG, "=== GEMINI API CALL END [$requestId] ===")

            curatedText
        } catch (e: Exception) {
            Log.e(TAG, "[$requestId] Error parsing response", e)
            "Gagal mendapatkan hasil dari Gemini."
        }
    }

    /**
     * Refines an existing post based on selected refinement options.
     */
    @Throws(Exception::class)
    override fun refinePost(originalPost: String, refinements: List<String>, includeSource: Boolean): String {
        val startTime = System.currentTimeMillis()
        val requestId = UUID.randomUUID().toString().substring(0, 8)

        Log.i(TAG, "=== GEMINI REFINEMENT START [$requestId] ===")
        Log.i(TAG, "[$requestId] Refinements: $refinements")
        Log.i(TAG, "[$requestId] Include source: $includeSource")

        val prompt = buildRefinementPrompt(originalPost, refinements, includeSource)
        val requestJson = buildRequestJson(prompt)
        val requestBodyString = gson.toJson(requestJson)

        val rawResult = try {
            val urlWithKey = "$endpoint?key=$apiKey"
            val body = requestBodyString.toRequestBody(JSON)
            val request = Request.Builder()
                .url(urlWithKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = sharedClient.newCall(request).execute()
            val code = response.code

            if (code >= 400) {
                val errorBody = response.body?.string() ?: ""
                response.close()
                throw Exception("Gemini API error: $code. $errorBody")
            }

            val result = response.body?.string()
            response.close()
            result
        } catch (ioe: IOException) {
            throw Exception("Network error: ${ioe.message}", ioe)
        }

        return try {
            val root = gson.fromJson(rawResult, JsonObject::class.java)
            var refinedText = extractTextFromJson(root)

            if (refinedText.isNullOrBlank()) {
                refinedText = "Gagal mendapatkan hasil dari Gemini."
            } else {
                refinedText = cleanUpResponse(refinedText)
                if (!includeSource) {
                    refinedText = removeSourceCitation(refinedText)
                }
            }

            val totalTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "[$requestId] Refinement success! (time: ${totalTime}ms)")
            Log.i(TAG, "=== GEMINI REFINEMENT END [$requestId] ===")

            refinedText
        } catch (e: Exception) {
            Log.e(TAG, "[$requestId] Error parsing refinement response", e)
            "Gagal mendapatkan hasil dari Gemini."
        }
    }

    private fun buildRequestJson(prompt: String): JsonObject {
        return JsonObject().apply {
            add("contents", JsonArray().apply {
                add(JsonObject().apply {
                    add("parts", JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("text", prompt)
                        })
                    })
                })
            })
        }
    }

    private fun calculateRetryDelay(
        lastException: Exception?,
        attempt: Int,
        rnd: Random,
        requestId: String
    ): Long {
        return if (lastException is RateLimitException) {
            val apiDelay = lastException.retryDelayMs
            if (apiDelay > 0) {
                val delay = minOf(MAX_DELAY_MS, apiDelay)
                Log.i(TAG, "[$requestId] Using API-suggested delay: ${delay}ms")
                delay
            } else {
                Log.i(TAG, "[$requestId] Using fallback delay for rate limit: ${RATE_LIMIT_FALLBACK_DELAY_MS}ms")
                RATE_LIMIT_FALLBACK_DELAY_MS
            }
        } else {
            var delay = minOf(MAX_DELAY_MS, BASE_DELAY_MS * (1L shl (attempt - 1)))
            val jitter = (rnd.nextDouble() * 500L).toLong()
            delay += jitter
            delay
        }
    }

    private fun removeSourceCitation(text: String?): String {
        if (text.isNullOrEmpty()) return text ?: ""

        var cleaned = SOURCE_CITATION_PATTERN.matcher(text).replaceAll("")
        if (text != cleaned) {
            Log.d(TAG, "Removed source citation via regex")
        }

        return TRAILING_NEWLINES_PATTERN.matcher(cleaned).replaceAll("").trim()
    }

    private fun buildRefinementPrompt(
        originalPost: String,
        refinements: List<String>,
        includeSource: Boolean
    ): String {
        val prompt = StringBuilder()
        prompt.append("You are refining a Malaysian Malay (Bahasa Malaysia) social media post about football. ")
        prompt.append("Apply the following improvements to the post:\n\n")

        for (refinement in refinements) {
            when (refinement) {
                "rephrase" -> prompt.append("- Rephrase: Rewrite the post with different wording while maintaining the same meaning and facts\n")
                "recheck_flow" -> prompt.append("- Recheck Flow: Improve the logical flow and structure of ideas\n")
                "recheck_wording" -> prompt.append("- Recheck Wording: Improve word choice and phrasing for better clarity\n")
                "formal" -> prompt.append("- Make it more Formal: Use formal language suitable for official club communication\n")
                "conversational" -> prompt.append("- Make it more Conversational: Use engaging, conversational tone suitable for fan communities\n")
                "shorten_detailed" -> prompt.append("- Shorten But Detailed: Make the post more concise while retaining all important details, facts, and key information. Remove redundant or filler words but keep the substance.\n")
                else -> {
                    if (refinement.isNotBlank()) {
                        prompt.append("- Custom Instruction: $refinement\n")
                    }
                }
            }
        }

        prompt.append("\nORIGINAL POST:\n---\n")
        prompt.append(originalPost)
        prompt.append("\n---\n\n")
        prompt.append("Provide ONLY the refined Bahasa Malaysia post. ")
        prompt.append("Maintain the same length and structure. ")
        prompt.append("Do NOT include any hashtags or explanations. ")
        prompt.append("Do NOT include any emojis in the output.")

        if (includeSource) {
            prompt.append("\nEnsure the post ends with 'Sumber: [Source Name]' if the original post had one or if the source is known.")
        } else {
            prompt.append("\nDo NOT include any 'Sumber:' citation in the output. Do NOT mention the source name, publication, or author anywhere in the post.")
        }

        return prompt.toString()
    }

    private fun extractTextFromJson(root: JsonObject?): String? {
        if (root == null) return null

        return try {
            // Try: candidates[0].content.parts[0].text
            if (root.has("candidates")) {
                val candidates = root.getAsJsonArray("candidates")
                if (candidates.size() > 0) {
                    val firstCandidate = candidates[0].asJsonObject
                    if (firstCandidate.has("content")) {
                        val content = firstCandidate.getAsJsonObject("content")
                        if (content.has("parts")) {
                            val parts = content.getAsJsonArray("parts")
                            if (parts.size() > 0) {
                                val firstPart = parts[0].asJsonObject
                                if (firstPart.has("text")) {
                                    val text = firstPart.get("text").asString
                                    if (!text.isNullOrBlank()) return text
                                }
                            }
                        }
                    }
                }
            }
            findFirstTextField(root)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from JSON", e)
            null
        }
    }

    private fun findFirstTextField(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null

        if (element.isJsonObject) {
            val obj = element.asJsonObject
            for (key in obj.keySet()) {
                if (key.equals("text", ignoreCase = true) && obj.get(key).isJsonPrimitive) {
                    val text = obj.get(key).asString
                    if (!text.isNullOrBlank()) return text
                }
                val deeper = findFirstTextField(obj.get(key))
                if (!deeper.isNullOrBlank()) return deeper
            }
        } else if (element.isJsonArray) {
            val arr = element.asJsonArray
            for (item in arr) {
                val deeper = findFirstTextField(item)
                if (!deeper.isNullOrBlank()) return deeper
            }
        }

        return null
    }

    private fun cleanUpResponse(response: String?): String {
        if (response.isNullOrBlank()) return response ?: ""

        var cleaned = response.trim()

        // Remove horizontal rule markers
        cleaned = HORIZONTAL_RULE_PATTERN.matcher(cleaned).replaceAll("")

        // Remove unwanted explanatory phrases
        for (phrase in UNWANTED_PHRASES) {
            cleaned = cleaned.replace(phrase, "")
        }

        // Remove text between asterisks
        cleaned = ASTERISK_TEXT_PATTERN.matcher(cleaned).replaceAll("")

        // Clean up spacing
        cleaned = MULTIPLE_NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n")
        cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ")
        cleaned = cleaned.trim()

        // If too short after cleaning, return original
        if (cleaned.length < 50) {
            Log.w(TAG, "Response too short after cleaning, returning original")
            return response
        }

        return cleaned
    }

    private fun parseRetryDelay(errorBody: String?, requestId: String): Long {
        try {
            if (errorBody.isNullOrBlank()) return 0

            val errorJson = gson.fromJson(errorBody, JsonObject::class.java)
            if (errorJson == null || !errorJson.has("error")) return 0

            val error = errorJson.getAsJsonObject("error")
            if (!error.has("details")) return 0

            val details = error.getAsJsonArray("details")
            for (detail in details) {
                val detailObj = detail.asJsonObject
                if (detailObj.has("@type") && detailObj.get("@type").asString.contains("RetryInfo")) {
                    if (detailObj.has("retryDelay")) {
                        val retryDelayStr = detailObj.get("retryDelay").asString
                        return parseRetryDelayString(retryDelayStr)
                    }
                }
            }

            return 0
        } catch (e: Exception) {
            Log.w(TAG, "[$requestId] Error parsing retry delay: ${e.message}")
            return 0
        }
    }

    private fun parseRetryDelayString(delayStr: String?): Long {
        if (delayStr.isNullOrEmpty()) return 0

        return try {
            val secondsStr = delayStr.replace(Regex("[^0-9.]"), "")
            val seconds = secondsStr.toDouble()
            (seconds * 1000).toLong()
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse delay string: $delayStr")
            0
        }
    }

    private fun extractUsageMetadata(root: JsonObject) {
        try {
            if (root.has("usageMetadata")) {
                val usage = root.getAsJsonObject("usageMetadata")

                if (usage.has("promptTokenCount")) {
                    _lastPromptTokens = usage.get("promptTokenCount").asInt
                }
                if (usage.has("candidatesTokenCount")) {
                    _lastCandidateTokens = usage.get("candidatesTokenCount").asInt
                }
                if (usage.has("totalTokenCount")) {
                    _lastTotalTokens = usage.get("totalTokenCount").asInt
                }

                Log.i(TAG, "Token usage - Prompt: $_lastPromptTokens, Response: $_lastCandidateTokens, Total: $_lastTotalTokens")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting usage metadata: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "GeminiService"
        private val JSON = "application/json; charset=utf-8".toMediaType()

        // Pre-compiled Regex Patterns
        private val HORIZONTAL_RULE_PATTERN: Pattern = Pattern.compile("(?m)^-{3,}\\s*$")
        private val ASTERISK_TEXT_PATTERN: Pattern = Pattern.compile("\\*.*?\\*")
        private val MULTIPLE_NEWLINES_PATTERN: Pattern = Pattern.compile("\\n\\s*\\n\\s*\\n+")
        private val HORIZONTAL_WHITESPACE_PATTERN: Pattern = Pattern.compile("[ \\t]+")
        private val SOURCE_CITATION_PATTERN: Pattern = Pattern.compile("(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$")
        private val TRAILING_NEWLINES_PATTERN: Pattern = Pattern.compile("\\n+$")

        // Tactical keywords for content detection
        private val TACTICAL_KEYWORDS = arrayOf(
            "formation", "tactical", "pressing", "possession", "xg", "expected goals",
            "pass completion", "progressive passes", "defensive line", "build-up",
            "counter-attack", "high press", "low block", "transition", "shape",
            "midfielder", "forward", "defender", "fullback", "winger",
            "4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2", "3-4-3"
        )

        // Unwanted phrases to remove from AI output
        private val UNWANTED_PHRASES = arrayOf(
            "Okay, ini percubaan untuk mengubah teks tersebut",
            "terjemahkan ke Bahasa Melayu (Malaysia)",
            "suntikkan sedikit gaya yang kurang formal",
            "istilah bola sepak Inggeris yang biasa",
            "Saya cuba gunakan perkataan yang lebih santai",
            "Saya juga masukkan istilah bola sepak",
            "Struktur diubah dengan menggabungkan",
            "Em dash (—) dibuang seperti yang diminta",
            "Tukar perkataan dari bahasa inggeris",
            "Semoga ini membantu",
            "Saya cuba",
            "Saya juga",
            "Struktur diubah",
            "Em dash",
            "Tukar perkataan",
            "Semoga ini"
        )

        // Retry configuration
        private const val MAX_RETRIES = 4
        private const val BASE_DELAY_MS = 500L
        private const val MAX_DELAY_MS = 60000L
        private const val RATE_LIMIT_FALLBACK_DELAY_MS = 30000L

        // Shared OkHttpClient
        private val sharedClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }
}
