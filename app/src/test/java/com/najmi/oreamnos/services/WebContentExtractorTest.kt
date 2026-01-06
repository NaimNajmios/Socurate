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
        val content = "Paragraph 1.   \n\n   Paragraph 2.\n\n\nParagraph 3."

        // 1. Replace horizontal whitespace
        var cleaned = HORIZONTAL_WHITESPACE_PATTERN.matcher(content).replaceAll(" ")

        // 2. Reduce multiple newlines
        cleaned = NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n")

        // We expect: "Paragraph 1. \n\n Paragraph 2.\n\nParagraph 3."
        // Note: The spaces before/after newlines might remain as single spaces depending on exact regex match,
        // but the key is that newlines exist.

        assertTrue("Output should contain newlines", cleaned.contains("\n"))

        // Verify specifically that we have 3 paragraphs separated by newlines
        val paragraphs = cleaned.split("\n+")
        assertEquals("Should have 3 paragraphs", 3, paragraphs.size)
    }
}
