package com.najmi.oreamnos.utils;

import java.util.regex.Pattern;

/**
 * Utility class for calculating readability scores.
 */
public class ReadabilityUtils {

    // Pre-compiled patterns for performance
    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("[.!?]+");
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");
    // NON_ALPHA_PATTERN and VOWEL_PATTERN removed as they are no longer used in the optimized countSyllables method
    // private static final Pattern NON_ALPHA_PATTERN = Pattern.compile("[^a-z]");
    // private static final Pattern VOWEL_PATTERN = Pattern.compile("[aeiouy]+");

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
     * <p>
     * ⚡ Bolt Optimization:
     * This method was optimized to avoid regex allocation (Pattern/Matcher) and String manipulation
     * (toLowerCase, replaceAll, substring) which creates excessive garbage.
     * It now uses a single-pass character iteration with O(1) memory usage.
     */
    public static int countSyllables(String word) {
        if (word == null || word.isEmpty()) {
            return 0;
        }

        int len = word.length();
        int cleanLen = 0;
        int lastAlphaIndex = -1;
        char lastAlphaChar = 0;

        // Pass 1: Scan for alpha characters and determine "clean" length
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            // Check if alpha (a-z or A-Z)
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                cleanLen++;
                lastAlphaIndex = i;
                lastAlphaChar = c;
            }
        }

        if (cleanLen == 0) {
            return 0;
        }

        if (cleanLen <= 3) {
            return 1;
        }

        // Determine effective end index for vowel counting
        // If the last alpha char is 'e' or 'E', we ignore it (silent 'e')
        int effectiveEndIndex = len;

        // Check if last alpha char is 'e' or 'E'
        if (lastAlphaChar == 'e' || lastAlphaChar == 'E') {
            // We want to stop processing before this character
            // Since we iterate up to i < effectiveEndIndex, we set it to lastAlphaIndex
            effectiveEndIndex = lastAlphaIndex;
        }

        int count = 0;
        boolean prevWasVowel = false;

        // Pass 2: Count vowel groups
        for (int i = 0; i < effectiveEndIndex; i++) {
            char c = word.charAt(i);

            // Skip non-alpha
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                continue;
            }

            // Check if vowel
            // Vowels: a, e, i, o, u, y (case insensitive)
            boolean isVowel = isVowel(c);

            if (isVowel) {
                if (!prevWasVowel) {
                    count++;
                }
                prevWasVowel = true;
            } else {
                prevWasVowel = false;
            }
        }

        // Adjust for specific cases if needed, but this is a standard approximation
        return Math.max(1, count);
    }

    /**
     * Helper to check if a char is a vowel.
     */
    private static boolean isVowel(char c) {
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y' ||
               c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y';
    }
}
