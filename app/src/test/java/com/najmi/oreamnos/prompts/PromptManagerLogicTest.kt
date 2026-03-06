package com.najmi.oreamnos.prompts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptManagerLogicTest {

    @Test
    fun `test years are NOT detected as bullet points`() {
        val text = "2024. The year of AI.\nThis should not be a list."
        assertFalse("Year should not be detected as bullet point", PromptManager.containsBulletPoints(text))
    }

    @Test
    fun `test standard numbered list IS detected`() {
        val text = "1. First item\n2. Second item"
        assertTrue("Standard list should be detected", PromptManager.containsBulletPoints(text))
    }

    @Test
    fun `test 3 digit number IS detected`() {
        val text = "999. Last item"
        assertTrue("3 digit number should be detected", PromptManager.containsBulletPoints(text))
    }

    @Test
    fun `test 4 digit number is NOT detected`() {
        val text = "1000. Not a list item"
        assertFalse("4 digit number should not be detected", PromptManager.containsBulletPoints(text))
    }

    @Test
    fun `test mixed content with year`() {
        // This simulates a news article starting with a year
        val text = "2023. A remarkable year for football.\nThe team played well."
        assertFalse("Text starting with year should not be flagged as list", PromptManager.containsBulletPoints(text))
    }
}
