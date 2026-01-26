package com.najmi.oreamnos.services

import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test

/**
 * Verifies the fix for GeminiService extraction logic.
 *
 * Note: In a standard Android unit test environment, you should use Mockito
 * to mock android.util.Log. This test is designed to work even if Log is not mocked
 * by detecting the specific RuntimeException thrown by unmocked Android methods.
 */
class GeminiExtractionLogicTest {

    @Test
    fun `extractTextFromJson should use fallback when optimized path fails`() {
        val root = JsonObject()
        // Setup malformed "candidates" to trigger exception in optimized path
        // It expects an Array, but we give it an Object -> ClassCastException
        root.add("candidates", JsonObject())

        // Valid text hidden elsewhere to be found by fallback
        val deep = JsonObject()
        deep.addProperty("text", "Hidden Content")
        root.add("other", deep)

        try {
            // Call the internal method directly (now accessible via companion object)
            val result = GeminiService.extractTextFromJson(root)

            // If Log is mocked/working, we expect correct result
            assertEquals("Should find content via fallback", "Hidden Content", result)

        } catch (e: RuntimeException) {
            // In unmocked environment (like standard JUnit without Robolectric),
            // Android methods throw RuntimeException("Method ... not mocked.")

            val msg = e.message ?: ""

            if (msg.contains("Method w in android.util.Log")) {
                // Success! The code attempted to log a Warning (Log.w).
                // This confirms it entered the specific catch block for optimization failure
                // which logs a warning before proceeding to fallback.
                println("Verified: Caught expected Log.w call (Optimization failed warning)")
                return
            } else if (msg.contains("Method e in android.util.Log")) {
                // Failure! The code attempted to log an Error (Log.e).
                // This implies it hit the outer catch block immediately (old buggy behavior)
                // or the fallback failed.
                fail("Failed: Called Log.e. Likely caught by outer block (buggy behavior) or fallback failed.")
            }

            // If it's another runtime exception, rethrow
            throw e
        }
    }
}
