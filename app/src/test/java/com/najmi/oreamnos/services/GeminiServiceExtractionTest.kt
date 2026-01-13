package com.najmi.oreamnos.services

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeminiServiceExtractionTest {

    @Test
    fun `extractTextFromJson returns text from valid standard response`() {
        val jsonString = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "Correct Output"
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val result = GeminiService.extractTextFromJson(jsonObject)

        assertEquals("Correct Output", result)
    }

    @Test
    fun `extractTextFromJson falls back to deep search when candidates is not an array`() {
        // This simulates the bug scenario: candidates exists but is an object, not an array.
        // The strict path expects an array and would throw IllegalStateException/ClassCastException.
        // The fallback should find the "text" field inside.
        val jsonString = """
            {
              "candidates": {
                 "something_else": "value",
                 "deep": {
                    "text": "Fallback Output"
                 }
              }
            }
        """.trimIndent()

        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val result = GeminiService.extractTextFromJson(jsonObject)

        assertEquals("Fallback Output", result)
    }

    @Test
    fun `extractTextFromJson returns null for empty candidates`() {
        val jsonString = """
            {
              "candidates": []
            }
        """.trimIndent()

        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val result = GeminiService.extractTextFromJson(jsonObject)

        assertNull(result)
    }

    @Test
    fun `extractTextFromJson finds text in arbitrary structure via fallback`() {
        val jsonString = """
            {
              "error": {
                 "details": [
                    {
                       "text": "Error Message"
                    }
                 ]
              }
            }
        """.trimIndent()

        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val result = GeminiService.extractTextFromJson(jsonObject)

        assertEquals("Error Message", result)
    }
}
