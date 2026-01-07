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
                    append("• ") // Convert to bullet
                    parseInlineFormatting(text, bulletContentStart, lineEnd)
                }
                // Check for already-bulleted line (• U+2022)
                else if (text.startsWith("\u2022", contentStart)) {
                    // Handle "• " (with space) or "•" (no space)
                    var bulletContentStart = contentStart + 1 // "•".length
                    if (bulletContentStart < lineEnd && text[bulletContentStart] == ' ') {
                        bulletContentStart++
                    }

                    append("• ")
                    parseInlineFormatting(text, bulletContentStart, lineEnd)
                }
                else {
                    // Normal line - parse inline formatting for the whole line content
                    parseInlineFormatting(text, index, lineEnd)
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
     * Optimized to avoid allocations by passing range indices
     */
    private fun AnnotatedString.Builder.parseInlineFormatting(text: String, start: Int, end: Int) {
        var currentIndex = start

        while (currentIndex < end) {
            // Find next marker: *, _, or **
            // We scan once instead of multiple times
            var markerIndex = -1
            var markerType = "" // "bold", "italic_star", "italic_under"

            // Scan loop to find the earliest marker
            var i = currentIndex
            while (i < end) {
                val c = text[i]

                if (c == '*') {
                    if (i + 1 < end && text[i + 1] == '*') {
                        markerIndex = i
                        markerType = "bold"
                        break
                    } else {
                        markerIndex = i
                        markerType = "italic_star"
                        break
                    }
                } else if (c == '_') {
                    if (i + 1 < end && text[i + 1] == '_') {
                        // Skip double underscore to match original logic of ignoring __
                        i += 2
                        continue
                    } else {
                        markerIndex = i
                        markerType = "italic_under"
                        break
                    }
                }
                i++
            }

            if (markerIndex == -1) {
                // No more markers
                append(text.substring(currentIndex, end))
                break
            }

            // Append text before marker
            if (markerIndex > currentIndex) {
                append(text.substring(currentIndex, markerIndex))
            }

            // Process the marker
            if (markerType == "bold") {
                val contentStart = markerIndex + 2
                val boldEnd = indexOf(text, "**", contentStart, end)
                if (boldEnd != -1) {
                    val boldText = text.substring(contentStart, boldEnd)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldText)
                    }
                    currentIndex = boldEnd + 2
                } else {
                    append("**")
                    currentIndex = contentStart
                }
            } else if (markerType == "italic_star") {
                val contentStart = markerIndex + 1
                val italicEnd = indexOf(text, "*", contentStart, end)
                if (italicEnd != -1) {
                    val italicText = text.substring(contentStart, italicEnd)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                    currentIndex = italicEnd + 1
                } else {
                    append("*")
                    currentIndex = contentStart
                }
            } else if (markerType == "italic_under") {
                val contentStart = markerIndex + 1
                val italicEnd = indexOf(text, "_", contentStart, end)
                if (italicEnd != -1) {
                    val italicText = text.substring(contentStart, italicEnd)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                    currentIndex = italicEnd + 1
                } else {
                    append("_")
                    currentIndex = contentStart
                }
            }
        }
    }

    /**
     * Optimized indexOf that checks upper bound
     */
    private fun indexOf(text: String, needle: String, start: Int, end: Int): Int {
        if (start >= end) return -1
        val idx = text.indexOf(needle, start)
        return if (idx != -1 && idx < end) idx else -1
    }
}
