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
            var nextMarker = -1
            var formatType = ""

            // Single pass scan to find the earliest marker
            // O(N) scan instead of repeated O(K*N) indexOf calls
            var scanIndex = currentIndex
            while (scanIndex < end) {
                val c = text[scanIndex]
                if (c == '*') {
                    if (scanIndex + 1 < end && text[scanIndex + 1] == '*') {
                        nextMarker = scanIndex
                        formatType = "bold"
                        break
                    } else {
                        nextMarker = scanIndex
                        formatType = "italic_star"
                        break
                    }
                } else if (c == '_') {
                    if (scanIndex + 1 < end && text[scanIndex + 1] == '_') {
                        // Double underscore, skip this char and the next
                        scanIndex += 2
                        continue
                    } else {
                        nextMarker = scanIndex
                        formatType = "italic_under"
                        break
                    }
                }
                scanIndex++
            }

            if (nextMarker == -1) {
                // No more formatting, append rest
                append(text.substring(currentIndex, end))
                break
            }

            // Append text before formatting
            append(text.substring(currentIndex, nextMarker))

            when (formatType) {
                "bold" -> {
                    val boldEnd = indexOf(text, "**", nextMarker + 2, end)
                    if (boldEnd != -1) {
                        val boldText = text.substring(nextMarker + 2, boldEnd)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(boldText)
                        }
                        currentIndex = boldEnd + 2
                    } else {
                        // Unclosed bold, append the marker and continue
                        append("**")
                        currentIndex = nextMarker + 2
                    }
                }
                "italic_star" -> {
                    val italicEnd = indexOf(text, "*", nextMarker + 1, end)
                    if (italicEnd != -1) {
                        val italicText = text.substring(nextMarker + 1, italicEnd)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(italicText)
                        }
                        currentIndex = italicEnd + 1
                    } else {
                        // Unclosed italic, append the marker and continue
                        append("*")
                        currentIndex = nextMarker + 1
                    }
                }
                "italic_under" -> {
                    val italicEnd = indexOf(text, "_", nextMarker + 1, end)
                    if (italicEnd != -1) {
                        val italicText = text.substring(nextMarker + 1, italicEnd)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(italicText)
                        }
                        currentIndex = italicEnd + 1
                    } else {
                        // Unclosed italic, append the marker and continue
                        append("_")
                        currentIndex = nextMarker + 1
                    }
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
