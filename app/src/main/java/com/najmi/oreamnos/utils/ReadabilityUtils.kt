package com.najmi.oreamnos.utils

import java.util.regex.Pattern
import kotlin.math.max

/**
 * Utility object for calculating readability scores.
 */
object ReadabilityUtils {

    private val WORD_SPLIT_PATTERN: Pattern = Pattern.compile("\\s+")

    /**
     * Calculates the Flesch-Kincaid Grade Level for the given text.
     * Formula: 0.39 * (total words / total sentences) + 11.8 * (total syllables / total words) - 15.59
     */
    @JvmStatic
    fun calculateFleschKincaidGradeLevel(text: String?): Double {
        if (text.isNullOrBlank()) return 0.0

        val totalSentences = countSentences(text)
        val words = WORD_SPLIT_PATTERN.split(text.trim())
        val totalWords = words.size
        val totalSyllables = countSyllablesInWords(words)

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
     */
    @JvmStatic
    fun countWords(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        return WORD_SPLIT_PATTERN.split(text.trim()).size
    }

    /**
     * Counts total syllables in an array of words.
     */
    private fun countSyllablesInWords(words: Array<String>): Int {
        return words.sumOf { countSyllables(it) }
    }

    /**
     * Counts syllables in a single word.
     * Uses a heuristic based on vowel groups.
     */
    @JvmStatic
    fun countSyllables(word: String?): Int {
        if (word.isNullOrEmpty()) return 0

        var effectiveLength = 0
        var lastAlphaIndex = -1

        // Pass 1: Calculate effective length (alpha chars only) and find last alpha char
        for (i in word.indices) {
            val c = word[i]
            if (c in 'a'..'z' || c in 'A'..'Z') {
                effectiveLength++
                lastAlphaIndex = i
            }
        }

        if (effectiveLength == 0) return 0
        if (effectiveLength <= 3) return 1

        // Check if the last alpha character is 'e' (silent e logic)
        val lastChar = word[lastAlphaIndex]
        val skipLast = lastChar == 'e' || lastChar == 'E'

        var count = 0
        var inVowel = false
        var processedAlpha = 0
        val limit = if (skipLast) effectiveLength - 1 else effectiveLength

        for (c in word) {
            if (processedAlpha >= limit) break

            if (c in 'a'..'z' || c in 'A'..'Z') {
                val lowerC = c.lowercaseChar()
                val isVowel = lowerC in "aeiouy"

                if (isVowel) {
                    if (!inVowel) {
                        count++
                        inVowel = true
                    }
                } else {
                    inVowel = false
                }
                processedAlpha++
            }
        }

        return max(1, count)
    }
}
