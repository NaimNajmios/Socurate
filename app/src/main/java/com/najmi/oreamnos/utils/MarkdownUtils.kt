package com.najmi.oreamnos.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Utility functions for parsing and rendering markdown text
 */
object MarkdownUtils {
    
    /**
     * Parses markdown formatting and converts to AnnotatedString for rich text display.
     * Supports: **bold**, *italic*, _italic_, ## Headers, - lists, * lists
     */
    @Composable
    fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split("\n")
            
            lines.forEachIndexed { lineIndex, line ->
                // Check for header (## Header)
                if (line.trimStart().startsWith("## ")) {
                    val headerText = line.trimStart().removePrefix("## ")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(headerText)
                    }
                }
                // Check for bullet list (- item or * item)
                else if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
                    val bulletText = line.trimStart().drop(2)
                    append("  • ") // Indent with bullet
                    parseInlineFormatting(bulletText)
                }
                else {
                    // Parse inline formatting (bold, italic)
                    parseInlineFormatting(line)
                }
                
                // Add newline except for last line
                if (lineIndex < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }
    
    /**
     * Helper function to parse inline formatting (bold and italic)
     */
    private fun AnnotatedString.Builder.parseInlineFormatting(text: String) {
        var currentIndex = 0
        
        while (currentIndex < text.length) {
            // Look for bold (**text**)
            val boldStart = text.indexOf("**", currentIndex)
            // Look for italic (*text* or _text_)
            val italicStarStart = text.indexOf("*", currentIndex).let { 
                if (it != -1 && it + 1 < text.length && text[it + 1] == '*') -1 else it 
            }
            val italicUnderStart = text.indexOf("_", currentIndex).let {
                if (it != -1 && it + 1 < text.length && text[it + 1] == '_') -1 else it
            }
            
            // Find earliest formatting marker
            val nextFormat = listOf(
                boldStart to "bold",
                italicStarStart to "italic_star",
                italicUnderStart to "italic_under"
            ).filter { it.first != -1 }.minByOrNull { it.first }
            
            if (nextFormat == null) {
                // No more formatting, append rest
                append(text.substring(currentIndex))
                break
            }
            
            val (formatStart, formatType) = nextFormat
            
            // Append text before formatting
            append(text.substring(currentIndex, formatStart))
            
            when (formatType) {
                "bold" -> {
                    val boldEnd = text.indexOf("**", formatStart + 2)
                    if (boldEnd != -1) {
                        val boldText = text.substring(formatStart + 2, boldEnd)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(boldText)
                        }
                        currentIndex = boldEnd + 2
                    } else {
                        append("**")
                        currentIndex = formatStart + 2
                    }
                }
                "italic_star" -> {
                    val italicEnd = text.indexOf("*", formatStart + 1)
                    if (italicEnd != -1) {
                        val italicText = text.substring(formatStart + 1, italicEnd)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(italicText)
                        }
                        currentIndex = italicEnd + 1
                    } else {
                        append("*")
                        currentIndex = formatStart + 1
                    }
                }
                "italic_under" -> {
                    val italicEnd = text.indexOf("_", formatStart + 1)
                    if (italicEnd != -1) {
                        val italicText = text.substring(formatStart + 1, italicEnd)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(italicText)
                        }
                        currentIndex = italicEnd + 1
                    } else {
                        append("_")
                        currentIndex = formatStart + 1
                    }
                }
            }
        }
    }
}
