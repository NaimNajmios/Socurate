package com.najmi.oreamnos.services

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern

class GeminiServiceCleanupTest {

    // Replicate the logic from GeminiService since cleanUpResponse is private
    // or test the regex directly since we cannot easily instantiate GeminiService
    // without mocking Android dependencies if it has them (Log, etc).
    // GeminiService uses android.util.Log which is mocked in unit tests usually,
    // but here we want to test the regex logic specifically.

    companion object {
        // The fixed regex from GeminiService
        private val ASTERISK_TEXT_PATTERN = Pattern.compile("\\*+(.*?)\\*+")

        fun cleanUpResponseSimulation(input: String): String {
            return ASTERISK_TEXT_PATTERN.matcher(input).replaceAll("$1")
        }
    }

    @Test
    fun `test cleanup preserves content in asterisks`() {
        val input = "This is *important* info."
        val expected = "This is important info."
        assertEquals(expected, cleanUpResponseSimulation(input))
    }

    @Test
    fun `test cleanup preserves bold content`() {
        val input = "Check out **this bold** text."
        val expected = "Check out this bold text."
        assertEquals(expected, cleanUpResponseSimulation(input))
    }

    @Test
    fun `test cleanup preserves source citation`() {
        val input = "Source: *Bernama*"
        val expected = "Source: Bernama"
        assertEquals(expected, cleanUpResponseSimulation(input))
    }

    @Test
    fun `test cleanup preserves score`() {
        val input = "Score: *2-1*"
        val expected = "Score: 2-1"
        assertEquals(expected, cleanUpResponseSimulation(input))
    }

    @Test
    fun `test cleanup preserves content with mixed formatting`() {
        val input = "Mixed *italic* and **bold** together."
        val expected = "Mixed italic and bold together."
        assertEquals(expected, cleanUpResponseSimulation(input))
    }
}
