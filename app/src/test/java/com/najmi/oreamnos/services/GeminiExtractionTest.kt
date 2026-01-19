package com.najmi.oreamnos.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeminiExtractionTest {

    @Test
    fun `test optimized path works for valid structure`() {
        // {"candidates": [{"content": {"parts": [{"text": "Hello World"}]}}]}
        val root = JsonObject()
        val candidates = JsonArray()
        val candidate = JsonObject()
        val content = JsonObject()
        val parts = JsonArray()
        val part = JsonObject()

        part.addProperty("text", "Hello World")
        parts.add(part)
        content.add("parts", parts)
        candidate.add("content", content)
        candidates.add(candidate)
        root.add("candidates", candidates)

        val result = GeminiService.extractTextFromJson(root)
        assertEquals("Hello World", result)
    }

    @Test
    fun `test fallback executes when optimized path fails`() {
        // "candidates" is NOT an array, which causes ClassCastException in optimized path
        // Text is hidden in another field which fallback should find
        val root = JsonObject()

        // Break optimized path: candidates is an object, not array
        val badCandidates = JsonObject()
        badCandidates.addProperty("error", "not an array")
        root.add("candidates", badCandidates)

        // Add valid text in a location fallback would find
        val validContent = JsonObject()
        validContent.addProperty("text", "Fallback Found Me")
        root.add("someOtherField", validContent)

        // Regression Test: extractTextFromJson should proceed to fallback when optimized path throws exception

        val result = GeminiService.extractTextFromJson(root)

        assertEquals("Fallback Found Me", result)
    }

    @Test
    fun `test fallback executes when optimized path returns null`() {
        // Optimized path runs fine but finds nothing (e.g. empty parts)
        // Fallback should run
        val root = JsonObject()
        val candidates = JsonArray()
        // Empty candidates array
        root.add("candidates", candidates)

        val validContent = JsonObject()
        validContent.addProperty("text", "Fallback Found Me")
        root.add("someOtherField", validContent)

        val result = GeminiService.extractTextFromJson(root)
        assertEquals("Fallback Found Me", result)
    }
}
