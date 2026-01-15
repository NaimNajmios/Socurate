package com.najmi.oreamnos.benchmark

import org.junit.Test
import kotlin.math.min

class MarkdownBenchmarkTest {

    interface MockBuilder {
        fun append(text: String)
        fun pushStyle(style: String)
        fun popStyle()
    }

    class TestBuilder : MockBuilder {
        val result = StringBuilder()

        override fun append(text: String) {
            result.append(text)
        }

        override fun pushStyle(style: String) {
            result.append("[$style]")
        }

        override fun popStyle() {
            result.append("[/]")
        }
    }

    class BenchmarkBuilder : MockBuilder {
        override fun append(text: String) {}
        override fun pushStyle(style: String) {}
        override fun popStyle() {}
    }

    private fun indexOf(text: String, needle: String, start: Int, end: Int): Int {
        if (start >= end) return -1
        val idx = text.indexOf(needle, start)
        return if (idx != -1 && idx < end) idx else -1
    }

    // --- Original Logic ---
    private fun MockBuilder.originalParseInlineFormatting(text: String, start: Int, end: Int) {
        var currentIndex = start

        while (currentIndex < end) {
            val boldStart = indexOf(text, "**", currentIndex, end)

            var italicStarStart = indexOf(text, "*", currentIndex, end)
            if (italicStarStart != -1 && italicStarStart + 1 < end && text[italicStarStart + 1] == '*') {
                italicStarStart = -1
            }

            val italicUnderStart = indexOf(text, "_", currentIndex, end).let {
                if (it != -1 && it + 1 < end && text[it + 1] == '_') -1 else it
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
                append(text.substring(currentIndex, end))
                break
            }

            append(text.substring(currentIndex, formatStart))

            when (formatType) {
                "bold" -> {
                    val boldEnd = indexOf(text, "**", formatStart + 2, end)
                    if (boldEnd != -1) {
                        val boldText = text.substring(formatStart + 2, boldEnd)
                        pushStyle("BOLD")
                        append(boldText)
                        popStyle()
                        currentIndex = boldEnd + 2
                    } else {
                        append("**")
                        currentIndex = formatStart + 2
                    }
                }
                "italic_star" -> {
                    val italicEnd = indexOf(text, "*", formatStart + 1, end)
                    if (italicEnd != -1) {
                        val italicText = text.substring(formatStart + 1, italicEnd)
                        pushStyle("ITALIC")
                        append(italicText)
                        popStyle()
                        currentIndex = italicEnd + 1
                    } else {
                        append("*")
                        currentIndex = formatStart + 1
                    }
                }
                "italic_under" -> {
                    val italicEnd = indexOf(text, "_", formatStart + 1, end)
                    if (italicEnd != -1) {
                        val italicText = text.substring(formatStart + 1, italicEnd)
                        pushStyle("ITALIC")
                        append(italicText)
                        popStyle()
                        currentIndex = italicEnd + 1
                    } else {
                        append("_")
                        currentIndex = formatStart + 1
                    }
                }
            }
        }
    }

    // --- Optimized Logic ---
    private fun MockBuilder.optimizedParseInlineFormatting(text: String, start: Int, end: Int) {
        var currentIndex = start

        while (currentIndex < end) {
            val starIdx = indexOf(text, "*", currentIndex, end)
            val underIdx = indexOf(text, "_", currentIndex, end)

            if (starIdx == -1 && underIdx == -1) {
                append(text.substring(currentIndex, end))
                break
            }

            val markerIdx = if (starIdx == -1) underIdx else if (underIdx == -1) starIdx else min(starIdx, underIdx)

            if (markerIdx > currentIndex) {
                append(text.substring(currentIndex, markerIdx))
            }

            var formatType = ""
            var markerLength = 0
            val c = text[markerIdx]

            if (c == '*') {
                if (markerIdx + 1 < end && text[markerIdx + 1] == '*') {
                    formatType = "bold"
                    markerLength = 2
                } else {
                    formatType = "italic_star"
                    markerLength = 1
                }
            } else { // _
                if (markerIdx + 1 < end && text[markerIdx + 1] == '_') {
                    // Double underscore - treat as literal
                    append("__")
                    currentIndex = markerIdx + 2
                    continue
                }
                formatType = "italic_under"
                markerLength = 1
            }

            // Find closer
            val contentStart = markerIdx + markerLength
            val closerNeedle = if (formatType == "bold") "**" else if (formatType == "italic_star") "*" else "_"

            val closerIndex = indexOf(text, closerNeedle, contentStart, end)

            if (closerIndex != -1) {
                val content = text.substring(contentStart, closerIndex)
                if (formatType == "bold") pushStyle("BOLD") else pushStyle("ITALIC")
                append(content)
                popStyle()
                currentIndex = closerIndex + markerLength
            } else {
                // No closer found, treat marker as literal
                append(text.substring(markerIdx, contentStart))
                currentIndex = contentStart
            }
        }
    }

    @Test
    fun benchmark() {
        // Verification
        verifyLogic("Hello **World**")
        verifyLogic("Simple text")
        verifyLogic("*Italic* text")
        verifyLogic("_Italic_ text")
        verifyLogic("Mixed **Bold** and *Italic*")
        verifyLogic("Unclosed **Bold")
        verifyLogic("Unclosed *Italic")
        verifyLogic("Nested **Bold *Italic* Bold**")
        verifyLogic("Double underscore __literal__")
        verifyLogic("***Triple***")

        println("Logic verification passed!")

        // Benchmark
        val sb = StringBuilder()
        for (i in 0 until 500) {
            sb.append("Here is some normal text with **bold words** and *italic words* and _underlines_. ")
            sb.append("Sometimes we have **unclosed formatting or ")
            sb.append("nested **things** inside. ")
            sb.append("Plain text with no formatting here just to test scanning speed. ")
        }
        val longText = sb.toString()
        val textLength = longText.length

        println("Text length: $textLength chars")

        val iterations = 500
        val builder = BenchmarkBuilder()

        // Warmup
        for (i in 0 until 50) {
            builder.originalParseInlineFormatting(longText, 0, textLength)
            builder.optimizedParseInlineFormatting(longText, 0, textLength)
        }

        val startOrig = System.nanoTime()
        for (i in 0 until iterations) {
            builder.originalParseInlineFormatting(longText, 0, textLength)
        }
        val endOrig = System.nanoTime()

        val startOpt = System.nanoTime()
        for (i in 0 until iterations) {
            builder.optimizedParseInlineFormatting(longText, 0, textLength)
        }
        val endOpt = System.nanoTime()

        val timeOrigMs = (endOrig - startOrig) / 1_000_000.0
        val timeOptMs = (endOpt - startOpt) / 1_000_000.0

        println("Original: ${timeOrigMs} ms")
        println("Optimized: ${timeOptMs} ms")
        println("Speedup: ${timeOrigMs / timeOptMs}x")
    }

    private fun verifyLogic(input: String) {
        val b1 = TestBuilder()
        val b2 = TestBuilder()

        b1.originalParseInlineFormatting(input, 0, input.length)
        b2.optimizedParseInlineFormatting(input, 0, input.length)

        if (b1.result.toString() != b2.result.toString()) {
            throw RuntimeException("Mismatch for '$input'!\nOriginal: ${b1.result}\nOptimized: ${b2.result}")
        }
    }
}
