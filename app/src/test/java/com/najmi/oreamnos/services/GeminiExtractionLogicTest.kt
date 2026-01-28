package com.najmi.oreamnos.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.RuntimeException

class GeminiExtractionLogicTest {

    /**
     * Verifies that if the "optimized" path fails (e.g. malformed JSON structure),
     * the code correctly falls back to searching specifically for a "text" field,
     * rather than giving up and returning null.
     */
    @Test
    fun testFallbackMechanism() {
        // Construct JSON where "candidates" is null (which causes getAsJsonArray to throw),
        // but a valid "text" field exists elsewhere in the structure.
        val jsonString = """
            {
                "candidates": null,
                "other_structure": {
                    "text": "Success"
                }
            }
        """.trimIndent()

        val root = Gson().fromJson(jsonString, JsonObject::class.java)

        try {
            val result = GeminiService.extractTextFromJson(root)

            // We expect the fallback logic (findFirstTextField) to find "Success"
            assertEquals("Success", result)

        } catch (e: RuntimeException) {
            // Handle the case where unit tests run against unmocked android.jar
            if (e.message?.startsWith("Method e in android.util.Log not mocked") == true) {
                 // If we hit this, it means the code entered the catch block which calls Log.e
                 // and returned null (implied).
                 // The fix should avoid logging in this specific catch block and proceed to fallback.
                 throw AssertionError("Bug reproduced: Fast path exception triggered Log.e and skipped fallback.", e)
            }
            throw e
        }
    }
}
