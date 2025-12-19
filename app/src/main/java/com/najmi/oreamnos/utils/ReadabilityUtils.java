package com.najmi.oreamnos.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for calculating readability scores.
 */
public class ReadabilityUtils {

    // Pre-compiled patterns for performance
    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("[.!?]+");
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");
    // Removed regex patterns used in countSyllables as they are replaced by loop implementation

    /**
     * Calculates the Flesch-Kincaid Grade Level for the given text.
     * Formula: 0.39 * (total words / total sentences) + 11.8 * (total syllables /
     * total words) - 15.59
     *
     * @param text The text to analyze.
     * @return The grade level score.
     */
    public static double calculateFleschKincaidGradeLevel(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        int totalSentences = countSentences(text);

        // Optimization: Split text into words once and reuse the array
        String[] words = WORD_SPLIT_PATTERN.split(text.trim());
        int totalWords = words.length;
        int totalSyllables = countSyllablesInWords(words);

        if (totalWords == 0 || totalSentences == 0) {
            return 0.0;
        }

        double score = (0.39 * ((double) totalWords / totalSentences)) +
                (11.8 * ((double) totalSyllables / totalWords)) - 15.59;

        // Clamp to 0
        return Math.max(0, score);
    }

    /**
     * Counts the number of sentences in the text.
     * Approximated by splitting by punctuation (. ! ?).
     */
    public static int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // Split by sentence terminators
        String[] sentences = SENTENCE_SPLIT_PATTERN.split(text);
        int count = 0;
        for (String s : sentences) {
            if (!s.trim().isEmpty()) {
                count++;
            }
        }
        return Math.max(1, count); // At least 1 sentence if text is not empty
    }

    /**
     * Counts the number of words in the text.
     */
    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = WORD_SPLIT_PATTERN.split(text.trim());
        return words.length;
    }

    /**
     * Counts total syllables in the text.
     */
    private static int countSyllablesInText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = WORD_SPLIT_PATTERN.split(text.trim());
        return countSyllablesInWords(words);
    }

    /**
     * Counts total syllables in an array of words.
     * Helper method to avoid re-splitting text.
     */
    private static int countSyllablesInWords(String[] words) {
        int count = 0;
        for (String word : words) {
            count += countSyllables(word);
        }
        return count;
    }

    /**
     * Counts syllables in a single word.
     * Uses a heuristic based on vowel groups.
     * Optimized to iterate characters directly, avoiding regex and object allocation.
     */
    public static int countSyllables(String word) {
        if (word == null || word.isEmpty()) {
            return 0;
        }

        int len = word.length();
        int effectiveLength = 0;
        int lastAlphaIndex = -1;

        // Pass 1: Calculate effective length (alpha chars only) and find last alpha char
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                effectiveLength++;
                lastAlphaIndex = i;
            }
        }

        if (effectiveLength == 0) return 0;
        if (effectiveLength <= 3) return 1;

        // Check if the last alpha character is 'e' (silent e logic)
        // If so, we effectively ignore it for vowel counting
        char lastChar = word.charAt(lastAlphaIndex);
        boolean skipLast = (lastChar == 'e' || lastChar == 'E');

        int count = 0;
        boolean inVowel = false;
        int processedAlpha = 0;
        // The limit of alpha characters to process
        int limit = skipLast ? effectiveLength - 1 : effectiveLength;

        for (int i = 0; i < len; i++) {
            // If we have processed all relevant alpha characters, stop
            if (processedAlpha >= limit) break;

            char c = word.charAt(i);
            // Quick check for alpha and normalize to lower case for vowel check
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                // 'a' | 32 gives 'a', 'A' | 32 gives 'a'
                char lowerC = (char) (c | 32);
                boolean isVowel = (lowerC == 'a' || lowerC == 'e' || lowerC == 'i' || lowerC == 'o' || lowerC == 'u' || lowerC == 'y');

                if (isVowel) {
                    if (!inVowel) {
                        count++;
                        inVowel = true;
                    }
                } else {
                    inVowel = false;
                }
                processedAlpha++;
            }
        }

        return Math.max(1, count);
    }
}
