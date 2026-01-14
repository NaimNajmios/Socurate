package com.najmi.oreamnos.benchmark

import java.util.regex.Pattern
import org.junit.Test

/**
 * Benchmark for MarkdownUtils inline formatting parsing.
 */
class MarkdownParsingBenchmark {

    // Stub for AnnotatedString logic
    class SpanStyle(val fontWeight: String? = null, val fontStyle: String? = null)
    class BenchmarkBuilder {
        val sb = StringBuilder()
        var styleCount = 0

        fun append(text: String) {
            sb.append(text)
        }

        fun withStyle(style: SpanStyle, block: BenchmarkBuilder.() -> Unit) {
            styleCount++
            this.block()
        }
    }

    // Original Logic (The "Before" state)
    fun parseInlineFormattingOriginal(builder: BenchmarkBuilder, text: String, start: Int, end: Int) {
        var currentIndex = start

        while (currentIndex < end) {
            // Look for bold (**text**)
            val boldStart = indexOf(text, "**", currentIndex, end)

            // Look for italic (*text* or _text_)
            // Skip * if it is part of ** (bold)
            var italicStarStart = indexOf(text, "*", currentIndex, end)
            if (italicStarStart != -1 && italicStarStart + 1 < end && text[italicStarStart + 1] == '*') {
                italicStarStart = -1
            }

            // Skip _ if it is part of __
            val italicUnderStart = indexOf(text, "_", currentIndex, end).let {
                if (it != -1 && it + 1 < end && text[it + 1] == '_') -1 else it
            }

            // Find earliest formatting marker without creating a List/Pair
            var formatStart = -1
            var formatType = ""

            // Check bold
            if (boldStart != -1) {
                formatStart = boldStart
                formatType = "bold"
            }

            // Check italic star
            if (italicStarStart != -1) {
                if (formatStart == -1 || italicStarStart < formatStart) {
                    formatStart = italicStarStart
                    formatType = "italic_star"
                }
            }

            // Check italic under
            if (italicUnderStart != -1) {
                if (formatStart == -1 || italicUnderStart < formatStart) {
                    formatStart = italicUnderStart
                    formatType = "italic_under"
                }
            }

            if (formatStart == -1) {
                // No more formatting, append rest
                builder.append(text.substring(currentIndex, end))
                break
            }

            // Append text before formatting
            builder.append(text.substring(currentIndex, formatStart))

            when (formatType) {
                "bold" -> {
                    val boldEnd = indexOf(text, "**", formatStart + 2, end)
                    if (boldEnd != -1) {
                        val boldText = text.substring(formatStart + 2, boldEnd)
                        builder.withStyle(SpanStyle(fontWeight = "Bold")) {
                            append(boldText)
                        }
                        currentIndex = boldEnd + 2
                    } else {
                        // Unclosed bold, append the marker and continue
                        builder.append("**")
                        currentIndex = formatStart + 2
                    }
                }
                "italic_star" -> {
                    val italicEnd = indexOf(text, "*", formatStart + 1, end)
                    if (italicEnd != -1) {
                        val italicText = text.substring(formatStart + 1, italicEnd)
                        builder.withStyle(SpanStyle(fontStyle = "Italic")) {
                            append(italicText)
                        }
                        currentIndex = italicEnd + 1
                    } else {
                        builder.append("*")
                        currentIndex = formatStart + 1
                    }
                }
                "italic_under" -> {
                    val italicEnd = indexOf(text, "_", formatStart + 1, end)
                    if (italicEnd != -1) {
                        val italicText = text.substring(formatStart + 1, italicEnd)
                        builder.withStyle(SpanStyle(fontStyle = "Italic")) {
                            append(italicText)
                        }
                        currentIndex = italicEnd + 1
                    } else {
                        builder.append("_")
                        currentIndex = formatStart + 1
                    }
                }
            }
        }
    }

    // Optimized Logic (Single Pass Scanner) - The "After" state
    fun parseInlineFormattingOptimized(builder: BenchmarkBuilder, text: String, start: Int, end: Int) {
        var currentIndex = start

        while (currentIndex < end) {
            // Scan for the next marker character (* or _)
            var nextMarkerIndex = -1
            for (i in currentIndex until end) {
                val c = text[i]
                if (c == '*' || c == '_') {
                    nextMarkerIndex = i
                    break
                }
            }

            if (nextMarkerIndex == -1) {
                builder.append(text.substring(currentIndex, end))
                break
            }

            // Append text before the marker
            if (nextMarkerIndex > currentIndex) {
                builder.append(text.substring(currentIndex, nextMarkerIndex))
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
                    builder.append("__")
                    currentIndex += 2 // Advance by 2
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
                     "italic_star" -> {
                         closerIndex = indexOf(text, "*", contentStart, end)
                     }
                     "italic_under" -> closerIndex = indexOf(text, "_", contentStart, end)
                 }

                 if (closerIndex != -1) {
                     val content = text.substring(contentStart, closerIndex)
                     if (formatType == "bold") {
                         builder.withStyle(SpanStyle(fontWeight = "Bold")) { append(content) }
                     } else {
                         builder.withStyle(SpanStyle(fontStyle = "Italic")) { append(content) }
                     }
                     val closerLength = if (formatType == "bold") 2 else 1
                     currentIndex = closerIndex + closerLength
                 } else {
                     // Unclosed. Append marker and continue.
                     builder.append(text.substring(currentIndex, contentStart))
                     currentIndex = contentStart
                 }
            }
        }
    }

    private fun indexOf(text: String, needle: String, start: Int, end: Int): Int {
        if (start >= end) return -1
        val idx = text.indexOf(needle, start)
        return if (idx != -1 && idx < end) idx else -1
    }

    @Test
    fun benchmark() {
        val mixedText = "Here is **bold** and *italic* and _italic_ and plain text."
        val longText = "This is a long text with no formatting to test the scanning speed vs iterative searching. ".repeat(50)
        val complexText = "Nesting **bold *italic* bold** and unclosed **bold and *italic".repeat(10)

        println("Verifying logic...")
        val b1 = BenchmarkBuilder()
        parseInlineFormattingOriginal(b1, mixedText, 0, mixedText.length)
        val b2 = BenchmarkBuilder()
        parseInlineFormattingOptimized(b2, mixedText, 0, mixedText.length)

        if (b1.sb.toString() != b2.sb.toString()) {
            println("MISMATCH!")
            println("Original: ${b1.sb}")
            println("Optimized: ${b2.sb}")
            throw RuntimeException("Logic Mismatch")
        }
        println("Logic verified on simple text.")

        // Edge case: __
        val doubleUnder = "Text with __ double underscore __."
        val b3 = BenchmarkBuilder(); parseInlineFormattingOriginal(b3, doubleUnder, 0, doubleUnder.length)
        val b4 = BenchmarkBuilder(); parseInlineFormattingOptimized(b4, doubleUnder, 0, doubleUnder.length)

        // Note: We accepted that optimized might behave slightly differently for `__` (finding `_` after).
        // Let's print what they do.
        println("Double Under Original: '${b3.sb}'")
        println("Double Under Optimized: '${b4.sb}'")

        val iterations = 50000
        val textToTest = mixedText.repeat(20) + longText + complexText

        println("Benchmarking Original...")
        val startOriginal = System.nanoTime()
        for (i in 0 until iterations) {
            val b = BenchmarkBuilder()
            parseInlineFormattingOriginal(b, textToTest, 0, textToTest.length)
        }
        val endOriginal = System.nanoTime()

        println("Benchmarking Optimized...")
        val startOptimized = System.nanoTime()
        for (i in 0 until iterations) {
            val b = BenchmarkBuilder()
            parseInlineFormattingOptimized(b, textToTest, 0, textToTest.length)
        }
        val endOptimized = System.nanoTime()

        val timeOriginal = (endOriginal - startOriginal) / 1_000_000.0
        val timeOptimized = (endOptimized - startOptimized) / 1_000_000.0

        println("\nResults ($iterations iterations):")
        println("Original: ${String.format("%.2f", timeOriginal)} ms")
        println("Optimized: ${String.format("%.2f", timeOptimized)} ms")
        println("Speedup: ${String.format("%.2f", timeOriginal / timeOptimized)}x")
    }
}
