package com.najmi.oreamnos.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the fallback logic in GeminiService.extractTextFromJson.
 *
 * Note: Since we are running on JVM without Android, we rely on
 * testOptions { unitTests.returnDefaultValues = true } to handle Log calls.
 */
class GeminiExtractionLogicTest {

    @Test
    fun `extractTextFromJson falls back to recursive search when strict path fails`() {
        // Arrange
        // Create a structure that fails the strict path but succeeds in fallback
        // Strict path expects: root["candidates"][0]["content"]["parts"][0]["text"]
        // We will make "candidates" NOT an array to trigger exception in strict path.

        val root = JsonObject()
        // Make "candidates" an Object instead of Array (triggers IllegalStateException/ClassCastException in getAsJsonArray)
        val candidatesObj = JsonObject()
        root.add("candidates", candidatesObj)

        // Put the text somewhere else where findFirstTextField can find it
        // e.g. root["someOtherPlace"]["text"] = "FoundIt"
        val otherPlace = JsonObject()
        otherPlace.addProperty("text", "FoundIt")
        root.add("someOtherPlace", otherPlace)

        // Act
        // Accessing the internal companion method
        val result = GeminiService.extractTextFromJson(root)

        // Assert
        assertEquals("FoundIt", result)
    }

    @Test
    fun `extractTextFromJson uses optimized path when structure matches`() {
        // Arrange
        val root = JsonObject()
        val candidates = JsonArray()
        val candidate = JsonObject()
        val content = JsonObject()
        val parts = JsonArray()
        val part = JsonObject()

        part.addProperty("text", "OptimizedResult")
        parts.add(part)
        content.add("parts", parts)
        candidate.add("content", content)
        candidates.add(candidate)
        root.add("candidates", candidates)

        // Act
        val result = GeminiService.extractTextFromJson(root)

        // Assert
        assertEquals("OptimizedResult", result)
    }
}
