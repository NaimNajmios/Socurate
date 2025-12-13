package com.najmi.oreamnos.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for calculating readability scores.
 */
public class ReadabilityUtils {

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
        int totalWords = countWords(text);
        int totalSyllables = countSyllablesInText(text);

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
        String[] sentences = text.split("[.!?]+");
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
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Counts total syllables in the text.
     */
    private static int countSyllablesInText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        int count = 0;
        for (String word : words) {
            count += countSyllables(word);
        }
        return count;
    }

    /**
     * Counts syllables in a single word.
     * Uses a heuristic based on vowel groups.
     */
    public static int countSyllables(String word) {
        if (word == null || word.isEmpty()) {
            return 0;
        }

        word = word.toLowerCase().replaceAll("[^a-z]", "");
        if (word.isEmpty()) {
            return 0;
        }

        if (word.length() <= 3) {
            return 1;
        }

        // Remove silent 'e' at the end
        if (word.endsWith("e")) {
            word = word.substring(0, word.length() - 1);
        }

        // Count vowel groups
        Pattern pattern = Pattern.compile("[aeiouy]+");
        Matcher matcher = pattern.matcher(word);
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        // Adjust for specific cases if needed, but this is a standard approximation
        return Math.max(1, count);
    }
}
