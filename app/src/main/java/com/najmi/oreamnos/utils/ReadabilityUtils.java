package com.najmi.oreamnos.utils;

import java.util.regex.Pattern;

/**
 * Utility class for calculating readability scores.
 * Optimized for performance by avoiding object allocations in hot paths.
 */
public class ReadabilityUtils {

    // Pre-compiled patterns for performance
    // Kept for legacy compatibility if needed, but not used in optimized methods
    // private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");

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

        // Single pass for words and syllables to avoid array allocation
        int totalWords = 0;
        int totalSyllables = 0;

        int len = text.length();
        boolean inWord = false;
        int wordStart = 0;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            boolean isWhitespace = Character.isWhitespace(c);

            if (!isWhitespace) {
                if (!inWord) {
                    inWord = true;
                    wordStart = i;
                }
            } else {
                if (inWord) {
                    inWord = false;
                    totalWords++;
                    totalSyllables += countSyllables(text, wordStart, i);
                }
            }
        }
        // Handle last word
        if (inWord) {
            totalWords++;
            totalSyllables += countSyllables(text, wordStart, len);
        }

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
     * Approximated by counting sentence terminators (. ! ?) and handling segments.
     * Optimized to avoid String.split() and array allocation.
     */
    public static int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        int count = 0;
        boolean hasContent = false;
        int len = text.length();

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            boolean isTerminator = (c == '.' || c == '!' || c == '?');

            if (isTerminator) {
                if (hasContent) {
                    count++;
                    hasContent = false;
                }
            } else {
                if (!Character.isWhitespace(c)) {
                    hasContent = true;
                }
            }
        }

        // If there is trailing content without a terminator, count it as a sentence
        if (hasContent) {
            count++;
        }

        return Math.max(1, count); // At least 1 sentence if text is not empty
    }

    /**
     * Counts the number of words in the text.
     * Optimized to avoid String.split() and array allocation.
     */
    public static int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int count = 0;
        boolean inWord = false;
        int len = text.length();

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c)) {
                if (!inWord) {
                    inWord = true;
                    count++;
                }
            } else {
                inWord = false;
            }
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
        return countSyllables(word, 0, word.length());
    }

    /**
     * Counts syllables in a specific range of the text.
     * Avoids creating substring objects.
     */
    public static int countSyllables(CharSequence text, int start, int end) {
        if (text == null || start >= end) {
            return 0;
        }

        int effectiveLength = 0;
        int lastAlphaIndex = -1;

        // Pass 1: Calculate effective length (alpha chars only) and find last alpha char
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                effectiveLength++;
                lastAlphaIndex = i;
            }
        }

        if (effectiveLength == 0) return 0;
        if (effectiveLength <= 3) return 1;

        // Check if the last alpha character is 'e' (silent e logic)
        char lastChar = text.charAt(lastAlphaIndex);
        boolean skipLast = (lastChar == 'e' || lastChar == 'E');

        int count = 0;
        boolean inVowel = false;
        int processedAlpha = 0;
        // The limit of alpha characters to process
        int limit = skipLast ? effectiveLength - 1 : effectiveLength;

        for (int i = start; i < end; i++) {
            // If we have processed all relevant alpha characters, stop
            if (processedAlpha >= limit) break;

            char c = text.charAt(i);
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
