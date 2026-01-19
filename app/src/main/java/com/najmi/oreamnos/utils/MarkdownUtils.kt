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
     * Optimized to use a single-pass character scan (O(N)) instead of repeated indexOf calls.
     */
    private fun AnnotatedString.Builder.parseInlineFormatting(text: String, start: Int, end: Int) {
        var i = start
        var lastPos = start

        while (i < end) {
            val c = text[i]

            // Check for markers: *, _
            if (c == '*' || c == '_') {
                var marker: String? = null
                var style: SpanStyle? = null
                var markerLen = 0

                // Check for bold "**"
                if (c == '*' && i + 1 < end && text[i + 1] == '*') {
                    marker = "**"
                    style = SpanStyle(fontWeight = FontWeight.Bold)
                    markerLen = 2
                }
                // Check for italic "*"
                else if (c == '*') {
                    marker = "*"
                    style = SpanStyle(fontStyle = FontStyle.Italic)
                    markerLen = 1
                }
                // Check for italic "_"
                else if (c == '_') {
                    if (i + 1 < end && text[i + 1] == '_') {
                        // Double underscore "__" - treat as literal text
                        i += 2
                        continue
                    }
                    marker = "_"
                    style = SpanStyle(fontStyle = FontStyle.Italic)
                    markerLen = 1
                }

                if (marker != null && style != null) {
                    // Append text pending so far
                    if (i > lastPos) {
                        append(text.substring(lastPos, i))
                    }

                    // Find closer
                    val contentStart = i + markerLen
                    val closerIdx = indexOf(text, marker, contentStart, end)

                    if (closerIdx != -1) {
                        // Found match
                        val content = text.substring(contentStart, closerIdx)
                        withStyle(style) {
                            append(content)
                        }
                        i = closerIdx + markerLen
                        lastPos = i
                        continue
                    } else {
                        // No match, treat marker as text
                        append(marker)
                        i += markerLen
                        lastPos = i
                        continue
                    }
                }
            }

            i++
        }

        // Append remaining
        if (lastPos < end) {
            append(text.substring(lastPos, end))
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
