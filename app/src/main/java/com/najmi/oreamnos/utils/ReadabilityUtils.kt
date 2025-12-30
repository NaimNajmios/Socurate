package com.najmi.oreamnos.utils

import java.util.regex.Pattern
import kotlin.math.max

/**
 * Utility object for calculating readability scores.
 */
object ReadabilityUtils {

    /**
     * Calculates the Flesch-Kincaid Grade Level for the given text.
     * Formula: 0.39 * (total words / total sentences) + 11.8 * (total syllables / total words) - 15.59
     *
     * Optimization: Uses a single pass character iteration to count words and syllables
     * without creating intermediate String or Array objects (no split()).
     */
    @JvmStatic
    fun calculateFleschKincaidGradeLevel(text: String?): Double {
        if (text.isNullOrBlank()) return 0.0

        val totalSentences = countSentences(text)

        var totalWords = 0
        var totalSyllables = 0
        var wordStart = -1

        val len = text.length
        for (i in 0 until len) {
            val c = text[i]
            if (!c.isWhitespace()) {
                if (wordStart == -1) {
                    wordStart = i
                }
            } else {
                if (wordStart != -1) {
                    // Word boundary found
                    totalSyllables += countSyllables(text, wordStart, i)
                    totalWords++
                    wordStart = -1
                }
            }
        }

        // Handle the last word
        if (wordStart != -1) {
            totalSyllables += countSyllables(text, wordStart, len)
            totalWords++
        }

        if (totalWords == 0 || totalSentences == 0) return 0.0

        val score = (0.39 * (totalWords.toDouble() / totalSentences)) +
                (11.8 * (totalSyllables.toDouble() / totalWords)) - 15.59

        return max(0.0, score)
    }

    /**
     * Counts the number of sentences in the text.
     */
    @JvmStatic
    fun countSentences(text: String?): Int {
        if (text.isNullOrBlank()) return 0

        var count = 0
        var hasContent = false

        for (c in text) {
            val isTerminator = c == '.' || c == '!' || c == '?'

            if (isTerminator) {
                if (hasContent) {
                    count++
                    hasContent = false
                }
            } else if (!c.isWhitespace()) {
                hasContent = true
            }
        }

        // If there is trailing content without a terminator, count it as a sentence
        if (hasContent) count++

        return max(1, count)
    }

    /**
     * Counts the number of words in the text.
     * Optimized to avoid array allocation from String.split().
     */
    @JvmStatic
    fun countWords(text: String?): Int {
        if (text.isNullOrBlank()) return 0

        var count = 0
        var inWord = false

        for (c in text) {
            if (c.isWhitespace()) {
                inWord = false
            } else if (!inWord) {
                inWord = true
                count++
            }
        }
        return count
    }

    /**
     * Counts syllables in a single word string.
     * Delegates to the efficient CharSequence version.
     */
    @JvmStatic
    fun countSyllables(word: String?): Int {
        if (word.isNullOrEmpty()) return 0
        return countSyllables(word, 0, word.length)
    }

    /**
     * Counts syllables in a substring of a CharSequence (zero-allocation).
     * Uses a single-pass heuristic based on vowel groups.
     * Optimized to avoid iterating the string multiple times.
     */
    fun countSyllables(text: CharSequence, start: Int, end: Int): Int {
        var count = 0
        var inVowel = false
        var effectiveLength = 0
        var lastAlphaChar = '\u0000'
        var lastGroupStartedByThisChar = false

        for (i in start until end) {
            val c = text[i]
            // Check alpha (ASCII only)
            if ((c in 'a'..'z') || (c in 'A'..'Z')) {
                effectiveLength++
                lastAlphaChar = c

                // Fast lowercasing for ASCII
                // 'A' is 65 (0x41), 'a' is 97 (0x61). Difference is 0x20.
                val lowerC = (c.code or 0x20).toChar()

                val isVowel = lowerC == 'a' || lowerC == 'e' || lowerC == 'i' ||
                        lowerC == 'o' || lowerC == 'u' || lowerC == 'y'

                if (isVowel) {
                    if (!inVowel) {
                        count++
                        inVowel = true
                        lastGroupStartedByThisChar = true
                    } else {
                        lastGroupStartedByThisChar = false
                    }
                } else {
                    inVowel = false
                    lastGroupStartedByThisChar = false
                }
            }
        }

        if (effectiveLength == 0) return 0
        if (effectiveLength <= 3) return 1

        // Handle silent 'e'
        // If last alpha is 'e', and it started its own vowel group (e.g., "ate", "rate"),
        // then it is likely silent, so we decrement.
        if ((lastAlphaChar == 'e' || lastAlphaChar == 'E') && lastGroupStartedByThisChar) {
            count--
        }

        return max(1, count)
    }
}
