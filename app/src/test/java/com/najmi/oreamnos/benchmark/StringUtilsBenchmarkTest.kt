package com.najmi.oreamnos.benchmark

import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * Benchmark test for StringUtils optimization.
 * This test verifies the performance improvement of using a fast-path check
 * before invoking Regex for emoji stripping.
 *
 * NOTE: This is a standalone benchmark file. In a real environment,
 * this would be part of the test suite or microbenchmark library.
 */
object StringUtilsBenchmarkTest {

    private const val EMOJI_PATTERN = "[" +
            "\u00a9\u00ae" + // © ®
            "\u2000-\u2021" + // Various symbols before bullet (U+2022 excluded)
            "\u2023-\u3300" + // Various symbols after bullet
            "\ud83c\ud000-\ud83c\udfff" + // Enclosed chars, flags, etc.
            "\ud83d\ud000-\ud83d\udfff" + // Emoticons, misc
            "\ud83e\ud000-\ud83e\udfff" + // Extended-A, chess, etc.
            "\u200d" + // ZWJ (zero-width joiner for compound emojis)
            "\ufe0f" + // Variation selector
            "]+"

    private val LEADING_EMOJI_PATTERN: Pattern = Pattern.compile("(?m)^($EMOJI_PATTERN)+\\s*")

    fun stripLeadingEmojisOriginal(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return LEADING_EMOJI_PATTERN.matcher(text).replaceAll("")
    }

    fun stripLeadingEmojisOptimized(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        // Fast path
        if (!containsPotentialEmoji(text)) {
             return text
        }
        return LEADING_EMOJI_PATTERN.matcher(text).replaceAll("")
    }

    private fun containsPotentialEmoji(text: String): Boolean {
        val len = text.length
        for (i in 0 until len) {
            val c = text[i]
            if (c == '\u00a9' || c == '\u00ae') return true
            if (c in '\u2000'..'\u3300') {
                if (c != '\u2022') return true
            }
            if (c in '\ud83c'..'\ud83e') return true
            if (c == '\u200d' || c == '\ufe0f') return true
        }
        return false
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val cleanText = "This is a clean text without any emojis. It represents a standard sentence in a post title or body."
        val emojiText = "🚀 This text starts with an emoji and has some more ⚽ here."

        println("Running basic logic verification...")
        // Verify logic is identical for no-emoji case
        val originalNoEmoji = stripLeadingEmojisOriginal("  Hello World")
        val optimizedNoEmoji = stripLeadingEmojisOptimized("  Hello World")

        if (originalNoEmoji != optimizedNoEmoji) {
            throw RuntimeException("Logic Mismatch! Original: '$originalNoEmoji', Optimized: '$optimizedNoEmoji'")
        }

        // Verify logic is identical for emoji case
        val originalEmoji = stripLeadingEmojisOriginal("🚀  Hello World")
        val optimizedEmoji = stripLeadingEmojisOptimized("🚀  Hello World")

         if (originalEmoji != optimizedEmoji) {
            throw RuntimeException("Logic Mismatch! Original: '$originalEmoji', Optimized: '$optimizedEmoji'")
        }
        println("Logic verification passed.")

        println("Warming up...")
        for (i in 0 until 100000) {
            stripLeadingEmojisOriginal(cleanText)
            stripLeadingEmojisOriginal(emojiText)
            stripLeadingEmojisOptimized(cleanText)
            stripLeadingEmojisOptimized(emojiText)
        }

        val iterations = 1000000

        println("Benchmarking Original (Clean Text)...")
        val startOriginalClean = System.nanoTime()
        for (i in 0 until iterations) {
            stripLeadingEmojisOriginal(cleanText)
        }
        val endOriginalClean = System.nanoTime()

        println("Benchmarking Optimized (Clean Text)...")
        val startOptimizedClean = System.nanoTime()
        for (i in 0 until iterations) {
            stripLeadingEmojisOptimized(cleanText)
        }
        val endOptimizedClean = System.nanoTime()

        val timeOriginalClean = (endOriginalClean - startOriginalClean) / 1_000_000.0
        val timeOptimizedClean = (endOptimizedClean - startOptimizedClean) / 1_000_000.0

        println("\nResults ($iterations iterations):")
        println("Clean Text - Original: ${String.format("%.2f", timeOriginalClean)} ms")
        println("Clean Text - Optimized: ${String.format("%.2f", timeOptimizedClean)} ms")
        println("Clean Text - Speedup: ${String.format("%.2f", timeOriginalClean / timeOptimizedClean)}x")
    }
}
