package com.najmi.oreamnos.curator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseCleanupTest {

    @Test
    fun `removeSourceCitation handles null input`() {
        val result = ResponseCleanup.removeSourceCitation(null)
        assertEquals("", result)
    }

    @Test
    fun `removeSourceCitation handles empty input`() {
        val result = ResponseCleanup.removeSourceCitation("")
        assertEquals("", result)
    }

    @Test
    fun `cleanUpResponse handles null input`() {
        val result = ResponseCleanup.cleanUpResponse(null)
        assertEquals("", result)
    }

    @Test
    fun `cleanUpResponse returns short responses unchanged`() {
        val shortText = "Short"
        val result = ResponseCleanup.cleanUpResponse(shortText)
        assertEquals(shortText, result)
    }

    @Test
    fun `cleanUpResponse removes horizontal rules`() {
        val text = "Content line 1\n---\nContent line 2\n---\nContent line 3\nMore content here to ensure it's long enough to pass the length check. Even more content."
        val result = ResponseCleanup.cleanUpResponse(text)
        assertTrue(!result.contains("---"))
    }

    @Test
    fun `cleanUpResponse removes excessive newlines`() {
        val text = "Line 1\n\n\n\n\nLine 2\n\n\n\nLine 3\nMore content here to ensure it's long enough to pass the length check. Even more content."
        val result = ResponseCleanup.cleanUpResponse(text)
        assertTrue(!result.contains("\n\n\n"))
    }

    @Test
    fun `cleanUpResponse removes horizontal whitespace`() {
        val text = "Word1    multiple   spaces   here. With extra text to pass the length check. Even more text to be safe."
        val result = ResponseCleanup.cleanUpResponse(text)
        assertTrue(!result.contains("   "))
    }

    @Test
    fun `cleanUpResponseWithMarkdown removes markdown bold markers`() {
        val text = "**Bold text** and more content to pass the length requirement. Even more text to be safe here."
        val result = ResponseCleanup.cleanUpResponseWithMarkdown(text)
        assertTrue(!result.contains("**"))
    }

    @Test
    fun `removeSourceCitation handles source keyword`() {
        val text = "Main content here with enough text. More content to ensure length. Even more content here.\n\nSource: BBC Sport\n\nMore text."
        val result = ResponseCleanup.removeSourceCitation(text)
        assertTrue(!result.contains("Source: BBC Sport"))
    }

    @Test
    fun `cleanUpResponse handles multilanguage content`() {
        val text = "JDT bertemu Selangor dalam aksi akhir musim ini. Stats: 65% possession, 12 shots, 7 corners. This is additional content to ensure it's long enough to pass the length check."
        val result = ResponseCleanup.cleanUpResponse(text)
        assertTrue(result.contains("JDT"))
        assertTrue(result.contains("Selangor"))
    }
}
