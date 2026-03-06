package com.najmi.oreamnos.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

/**
 * Unit tests for regex patterns used in WebContentExtractor.
 */
class WebContentExtractorTest {

    // Replicating the pattern from WebContentExtractor to verify the logic
    // Since the original pattern is private, we test the logic that we enforced.
    private val HORIZONTAL_WHITESPACE_PATTERN = Pattern.compile("[ \\t]+")
    private val NEWLINES_PATTERN = Pattern.compile("(?:\\n\\s*){3,}")

    @Test
    fun `test whitespace pattern preserves newlines`() {
        val content = "Paragraph 1.\n\nParagraph 2."
        val cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(content).replaceAll(" ")

        // Assert that newlines are NOT replaced by spaces
        assertTrue("Newlines should be preserved", cleaned.contains("\n"))
        assertEquals("Paragraph 1.\n\nParagraph 2.", cleaned)
    }

    @Test
    fun `test whitespace pattern collapses multiple spaces`() {
        val content = "Word1     Word2"
        val cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(content).replaceAll(" ")

        assertEquals("Word1 Word2", cleaned)
    }

    @Test
    fun `test full cleanup logic simulation`() {
        // App's cleanContent approach:
        // 1. HORIZONTAL_WHITESPACE_PATTERN: Replace [ \t]+ with " "
        // 2. NEWLINES_PATTERN: Replace (?:\n\s*){3,} with "\n\n"
        // 3. trim()
        val content = "Paragraph 1.   \n\n   Paragraph 2.\n\n\nParagraph 3."

        var cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(content).replaceAll(" ")
        cleaned = NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n")
        cleaned = cleaned.trim()

        // "Paragraph 1. \n\n Paragraph 2.\n\nParagraph 3."
        assertTrue("Output should contain newlines", cleaned.contains("\n"))

        // When splitting by 2 or more newlines, we should get 3 paragraphs
        val paragraphs = cleaned.split(Regex("\\n{2,}"))
        assertEquals("Should have 3 paragraphs", 3, paragraphs.size)
    }
}
