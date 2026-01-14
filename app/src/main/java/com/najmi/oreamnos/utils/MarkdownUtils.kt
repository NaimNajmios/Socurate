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
            // Scan for the next marker character (* or _)
            // Optimization: Single-pass scanning avoids O(N^2) behavior of repeated indexOf calls
            var nextMarkerIndex = -1
            for (i in currentIndex until end) {
                val c = text[i]
                if (c == '*' || c == '_') {
                    nextMarkerIndex = i
                    break
                }
            }

            if (nextMarkerIndex == -1) {
                append(text.substring(currentIndex, end))
                break
            }

            // Append text before the marker
            if (nextMarkerIndex > currentIndex) {
                append(text.substring(currentIndex, nextMarkerIndex))
            }

            currentIndex = nextMarkerIndex
            val c = text[currentIndex]

            // Determine marker type
            var formatType = ""
            var markerLength = 0

            if (c == '*') {
                if (currentIndex + 1 < end && text[currentIndex + 1] == '*') {
                    formatType = "bold"
                    markerLength = 2
                } else {
                    formatType = "italic_star"
                    markerLength = 1
                }
            } else if (c == '_') {
                if (currentIndex + 1 < end && text[currentIndex + 1] == '_') {
                    // Double underscore - ignore as marker (treat as text)
                    append("__")
                    currentIndex += 2
                    continue
                } else {
                    formatType = "italic_under"
                    markerLength = 1
                }
            }

            // Look for closer
            if (formatType.isNotEmpty()) {
                val contentStart = currentIndex + markerLength
                var closerIndex = -1

                when (formatType) {
                    "bold" -> closerIndex = indexOf(text, "**", contentStart, end)
                    "italic_star" -> closerIndex = indexOf(text, "*", contentStart, end)
                    "italic_under" -> closerIndex = indexOf(text, "_", contentStart, end)
                }

                if (closerIndex != -1) {
                    val content = text.substring(contentStart, closerIndex)
                    if (formatType == "bold") {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(content) }
                    } else {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(content) }
                    }

                    // Advance past the closing marker
                    val closerLength = if (formatType == "bold") 2 else 1
                    currentIndex = closerIndex + closerLength
                } else {
                    // Unclosed. Append marker and continue.
                    append(text.substring(currentIndex, contentStart))
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
