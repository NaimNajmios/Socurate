package com.najmi.oreamnos.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Reproduction test case for the JSON extraction bug in GeminiService.
 * Verified that exceptions in the optimized path cause the fallback logic to be skipped.
 */
class GeminiExtractionTest {

    // Simulate the BUGGY implementation from GeminiService.kt
    private fun extractTextFromJsonBuggy(root: JsonObject?): String? {
        if (root == null) return null

        return try {
            // OPTIMIZATION: Use safe direct access...
            // If this chain throws (e.g. ClassCastException, NPE), we jump to catch
            val text = root.getAsJsonArray("candidates")
                ?.takeIf { !it.isEmpty }
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.takeIf { !it.isEmpty }
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString

            if (!text.isNullOrBlank()) return text

            findFirstTextField(root)
        } catch (e: Exception) {
            // Log.e(TAG, "Error extracting text from JSON", e)
            println("Log.e: Error extracting text from JSON: ${e.message}")
            null // Fallback is SKIPPED here!
        }
    }

    // Fixed implementation
    private fun extractTextFromJsonFixed(root: JsonObject?): String? {
        if (root == null) return null

        val text = try {
            root.getAsJsonArray("candidates")
                ?.takeIf { !it.isEmpty }
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.takeIf { !it.isEmpty }
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString
        } catch (e: Exception) {
            // Log.w(TAG, "Optimized extraction failed...", e)
            println("Log.w: Optimized extraction failed, falling back: ${e.message}")
            null
        }

        if (!text.isNullOrBlank()) return text

        return findFirstTextField(root)
    }

    // Helper from GeminiService (assumed correct)
    private fun findFirstTextField(element: com.google.gson.JsonElement?): String? {
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

    @Test
    fun testExtractionBug_WhenOptimizedPathFailsWithException() {
        // Case: "candidates" exists but is NOT an array (e.g. Object), causing getAsJsonArray to throw
        val jsonStr = """
            {
                "candidates": {
                    "note": "This is an object, not an array. Causes crash in optimized path."
                },
                "fallback": {
                    "text": "Correct Content"
                }
            }
        """.trimIndent()

        val root = JsonParser.parseString(jsonStr).asJsonObject

        // BUGGY: Should return null because exception is caught and fallback is skipped
        val resultBuggy = extractTextFromJsonBuggy(root)
        println("Buggy Result: $resultBuggy")
        assertNull("Buggy implementation should fail to find text due to swallowed exception", resultBuggy)

        // FIXED: Should return "Correct Content" because it falls back
        val resultFixed = extractTextFromJsonFixed(root)
        println("Fixed Result: $resultFixed")
        assertEquals("Correct Content", resultFixed)
    }

    @Test
    fun testExtraction_NormalPath() {
        val jsonStr = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                { "text": "Optimized Content" }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val root = JsonParser.parseString(jsonStr).asJsonObject

        val resultFixed = extractTextFromJsonFixed(root)
        assertEquals("Optimized Content", resultFixed)
    }
}
