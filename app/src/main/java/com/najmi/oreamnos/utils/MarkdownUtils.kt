package com.najmi.oreamnos.utils

import androidx.compose.ui.graphics.Color
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
     *
     * OPTIMIZATION: Uses line-by-line scanning with index pointers instead of String.split("\n")
     * to avoid allocating a large List and many substrings.
     */
    fun parseMarkdownToAnnotatedString(text: String, primaryColor: Color): AnnotatedString {
        return buildAnnotatedString {
            val length = text.length
            var index = 0

            while (index < length) {
                // Find end of current line
                var lineEnd = text.indexOf('\n', index)
                if (lineEnd == -1) lineEnd = length

                // Calculate start of content (skipping whitespace)
                var contentStart = index
                while (contentStart < lineEnd && text[contentStart].isWhitespace()) {
                    contentStart++
                }

                // If line is empty or just whitespace
                if (contentStart == lineEnd) {
                    // Just append the content (which is empty) and the newline if needed
                }
                // Check for header (## Header)
                else if (text.startsWith("## ", contentStart)) {
                    // Extract content after "## "
                    val headerStart = contentStart + 3 // "## ".length
                    val headerText = if (headerStart < lineEnd) text.substring(headerStart, lineEnd) else ""

                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    ) {
                        append(headerText)
                    }
                }
                // Check for bullet list (- item, * item)
                else if (text.startsWith("- ", contentStart) || text.startsWith("* ", contentStart)) {
                    val bulletContentStart = contentStart + 2 // "- ".length
                    val bulletText = if (bulletContentStart < lineEnd) text.substring(bulletContentStart, lineEnd) else ""

                    append("• ") // Convert to bullet
                    parseInlineFormatting(bulletText)
                }
                // Check for already-bulleted line (• U+2022)
                else if (text.startsWith("\u2022", contentStart)) {
                    // Handle "• " (with space) or "•" (no space)
                    var bulletContentStart = contentStart + 1 // "•".length
                    if (bulletContentStart < lineEnd && text[bulletContentStart] == ' ') {
                        bulletContentStart++
                    }

                    val bulletText = if (bulletContentStart < lineEnd) text.substring(bulletContentStart, lineEnd) else ""
                    append("• ")
                    parseInlineFormatting(bulletText)
                }
                else {
                    // Normal line - parse inline formatting for the whole line content
                    // We use substring here because parseInlineFormatting expects a String
                    val lineContent = text.substring(index, lineEnd)
                    parseInlineFormatting(lineContent)
                }

                // Move to next line
                if (lineEnd < length) {
                    append("\n")
                    index = lineEnd + 1
                } else {
                    index = length
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
