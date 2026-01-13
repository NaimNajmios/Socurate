package com.najmi.oreamnos.benchmark

import org.junit.Test
import org.junit.Assert.assertEquals

class MarkdownBenchmarkTest {

    // Mock Builder
    class MockBuilder {
        val sb = StringBuilder()

        fun append(text: String) {
            sb.append(text)
        }

        fun withStyle(block: () -> Unit) {
            block()
        }
    }

    // Original Logic (ported to Kotlin for verification)
    private fun parseInlineFormattingOriginal(builder: MockBuilder, text: String, start: Int, end: Int) {
        var currentIndex = start
        while (currentIndex < end) {
            val boldStart = indexOf(text, "**", currentIndex, end)

            var italicStarStart = indexOf(text, "*", currentIndex, end)
            if (italicStarStart != -1 && italicStarStart + 1 < end && text[italicStarStart + 1] == '*') {
                italicStarStart = -1
            }

            var italicUnderStart = indexOf(text, "_", currentIndex, end)
             if (italicUnderStart != -1 && italicUnderStart + 1 < end && text[italicUnderStart + 1] == '_') {
                italicUnderStart = -1
            }

            var formatStart = -1
            var formatType = ""

            if (boldStart != -1) {
                formatStart = boldStart
                formatType = "bold"
            }

            if (italicStarStart != -1) {
                if (formatStart == -1 || italicStarStart < formatStart) {
                    formatStart = italicStarStart
                    formatType = "italic_star"
                }
            }

            if (italicUnderStart != -1) {
                if (formatStart == -1 || italicUnderStart < formatStart) {
                    formatStart = italicUnderStart
                    formatType = "italic_under"
                }
            }

            if (formatStart == -1) {
                builder.append(text.substring(currentIndex, end))
                break;
            }

            builder.append(text.substring(currentIndex, formatStart))

            if (formatType == "bold") {
                val boldEnd = indexOf(text, "**", formatStart + 2, end)
                if (boldEnd != -1) {
                    val boldText = text.substring(formatStart + 2, boldEnd)
                    builder.withStyle { builder.append(boldText) }
                    currentIndex = boldEnd + 2
                } else {
                    builder.append("**")
                    currentIndex = formatStart + 2
                }
            } else if (formatType == "italic_star") {
                val italicEnd = indexOf(text, "*", formatStart + 1, end)
                if (italicEnd != -1) {
                    val italicText = text.substring(formatStart + 1, italicEnd)
                    builder.withStyle { builder.append(italicText) }
                    currentIndex = italicEnd + 1
                } else {
                    builder.append("*")
                    currentIndex = formatStart + 1
                }
            } else if (formatType == "italic_under") {
                val italicEnd = indexOf(text, "_", formatStart + 1, end)
                if (italicEnd != -1) {
                    val italicText = text.substring(formatStart + 1, italicEnd)
                    builder.withStyle { builder.append(italicText) }
                    currentIndex = italicEnd + 1
                } else {
                    builder.append("_")
                    currentIndex = formatStart + 1
                }
            }
        }
    }

    private fun indexOf(text: String, needle: String, start: Int, end: Int): Int {
        if (start >= end) return -1
        val idx = text.indexOf(needle, start)
        return if (idx != -1 && idx < end) idx else -1
    }

    // Optimized Logic (The one used in MarkdownUtils)
    private fun parseInlineFormattingOptimized(builder: MockBuilder, text: String, start: Int, end: Int) {
        var i = start
        var lastAppendIndex = start

        while (i < end) {
            val c = text[i]

            // Potential start of formatting
            if (c == '*' || c == '_') {
                // Check for bold (** only)
                if (c == '*' && i + 1 < end && text[i + 1] == '*') {
                    // It is bold '**'
                    // Append pending text
                    if (i > lastAppendIndex) {
                        builder.append(text.substring(lastAppendIndex, i))
                    }

                    // Look for closing '**'
                    val boldContentStart = i + 2
                    val closeIndex = indexOf(text, "**", boldContentStart, end)

                    if (closeIndex != -1) {
                        // Found closing
                        val boldText = text.substring(boldContentStart, closeIndex)
                        builder.withStyle { builder.append(boldText) }
                        i = closeIndex + 2
                        lastAppendIndex = i
                        continue
                    } else {
                        // No closing, treat as text
                        builder.append("**")
                        i += 2
                        lastAppendIndex = i
                        continue
                    }
                }

                // Check for italic (* or _)
                var isItalicStart = true
                if (c == '_') {
                     // Check if it is __ (double underscore)
                     if (i + 1 < end && text[i + 1] == '_') {
                         isItalicStart = false
                         i++
                     }
                }

                if (isItalicStart) {
                    // Append pending
                    if (i > lastAppendIndex) {
                        builder.append(text.substring(lastAppendIndex, i))
                    }

                    val marker = if (c == '*') "*" else "_"
                    val italicContentStart = i + 1
                    val closeIndex = indexOf(text, marker, italicContentStart, end)

                    if (closeIndex != -1) {
                         val italicText = text.substring(italicContentStart, closeIndex)
                         builder.withStyle { builder.append(italicText) }
                         i = closeIndex + 1
                         lastAppendIndex = i
                         continue
                    } else {
                        builder.append(marker)
                        i += 1
                        lastAppendIndex = i
                        continue
                    }
                }
            }

            i++
        }

        // Append remaining
        if (lastAppendIndex < end) {
            builder.append(text.substring(lastAppendIndex, end))
        }
    }

    @Test
    fun testLogicParity() {
        val cleanText = "This is a normal sentence with no formatting whatsoever."
        val formatText = "This is **bold** and *italic* and _italic_ and **bold again**."
        val messyText = "Unclosed **bold and *nested* things."
        val underText = "This has __double underscores__ inside."

        verify(cleanText)
        verify(formatText)
        verify(messyText)
        verify(underText)
    }

    private fun verify(text: String) {
        val b1 = MockBuilder()
        parseInlineFormattingOriginal(b1, text, 0, text.length)

        val b2 = MockBuilder()
        parseInlineFormattingOptimized(b2, text, 0, text.length)

        assertEquals("Mismatch for input: $text", b1.sb.toString(), b2.sb.toString())
    }
}
