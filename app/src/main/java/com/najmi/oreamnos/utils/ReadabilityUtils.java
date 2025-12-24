package com.najmi.oreamnos.utils;

/**
 * Utility class for calculating readability scores.
 */
public class ReadabilityUtils {

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

        // Bolt Optimization: Avoid String.split() allocation
        int[] stats = countWordsAndSyllables(text);
        int totalWords = stats[0];
        int totalSyllables = stats[1];

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
     * Optimized to avoid String.split() allocation.
     */
    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        int count = 0;
        int len = text.length();
        boolean inWord = false;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                if (inWord) {
                    count++;
                    inWord = false;
                }
            } else {
                inWord = true;
            }
        }

        if (inWord) {
            count++;
        }

        return count;
    }

    /**
     * Counts words and syllables in one pass.
     * @return int[] where [0] is word count, [1] is total syllables
     */
    private static int[] countWordsAndSyllables(String text) {
        int wordCount = 0;
        int syllableCount = 0;
        int len = text.length();
        int wordStart = -1;

        for (int i = 0; i < len; i++) {
             char c = text.charAt(i);
             if (Character.isWhitespace(c)) {
                 if (wordStart != -1) {
                     wordCount++;
                     syllableCount += countSyllables(text, wordStart, i);
                     wordStart = -1;
                 }
             } else {
                 if (wordStart == -1) {
                     wordStart = i;
                 }
             }
        }

        if (wordStart != -1) {
             wordCount++;
             syllableCount += countSyllables(text, wordStart, len);
        }

        return new int[]{wordCount, syllableCount};
    }

    /**
     * Counts syllables in a single word (passed as String).
     * Maintains backward compatibility.
     */
    public static int countSyllables(String word) {
        if (word == null || word.isEmpty()) {
            return 0;
        }
        return countSyllables(word, 0, word.length());
    }

    /**
     * Counts syllables in a substring of text.
     * Uses a heuristic based on vowel groups.
     * Optimized to iterate characters directly, avoiding allocation.
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
        // If so, we effectively ignore it for vowel counting
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
