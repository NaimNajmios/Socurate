package com.najmi.oreamnos.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Validates the logic for extracting text from Gemini JSON responses.
 *
 * NOTE: This test calls [GeminiService.extractTextFromJson] which uses [android.util.Log].
 * To run this test successfully, you must have an environment that mocks Android classes
 * (e.g., Robolectric, or android.testOptions.unitTests.returnDefaultValues = true).
 */
@RunWith(JUnit4::class)
class GeminiExtractionLogicTest {

    @Test
    fun `test bug - fallback is triggered when optimization fails`() {
        // Construct JSON where 'candidates' is an Object (not Array), triggering ClassCastException
        // in the optimized path.
        val jsonString = """
            {
                "candidates": { "error": "Not an array" },
                "fallback": {
                    "text": "Correct Text"
                }
            }
        """.trimIndent()

        val root = Gson().fromJson(jsonString, JsonObject::class.java)

        // Call the internal companion method directly
        // This validates that the try-catch block in the actual code correctly swallows
        // the exception and proceeds to findFirstTextField.
        val result = GeminiService.extractTextFromJson(root)

        assertEquals("Correct Text", result)
    }

    @Test
    fun `test normal success with optimized path`() {
        val jsonString = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                { "text": "Optimized Text" }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val root = Gson().fromJson(jsonString, JsonObject::class.java)
        val result = GeminiService.extractTextFromJson(root)
        assertEquals("Optimized Text", result)
    }

    @Test
    fun `test deep fallback recursion`() {
        val jsonString = """
            {
                "some": {
                    "deep": {
                        "nested": {
                            "structure": {
                                "text": "Deep Text"
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        val root = Gson().fromJson(jsonString, JsonObject::class.java)
        val result = GeminiService.extractTextFromJson(root)
        assertEquals("Deep Text", result)
    }
}
