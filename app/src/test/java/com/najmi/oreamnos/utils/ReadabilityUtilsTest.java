package com.najmi.oreamnos.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReadabilityUtilsTest {

    @Test
    public void testCountSyllables() {
        assertEquals(1, ReadabilityUtils.countSyllables("a"));
        assertEquals(1, ReadabilityUtils.countSyllables("the"));
        assertEquals(2, ReadabilityUtils.countSyllables("hello"));
        assertEquals(3, ReadabilityUtils.countSyllables("beautiful"));
        assertEquals(1, ReadabilityUtils.countSyllables("code")); // Silent 'e'
        assertEquals(2, ReadabilityUtils.countSyllables("coding"));
        assertEquals(0, ReadabilityUtils.countSyllables(""));
        assertEquals(0, ReadabilityUtils.countSyllables(null));

        // Additional tests for robustness
        assertEquals(1, ReadabilityUtils.countSyllables("queue"));
        assertEquals(1, ReadabilityUtils.countSyllables("tree"));
        assertEquals(2, ReadabilityUtils.countSyllables("little"));
        assertEquals(3, ReadabilityUtils.countSyllables("performance"));
        assertEquals(5, ReadabilityUtils.countSyllables("optimization"));
        assertEquals(2, ReadabilityUtils.countSyllables("regex"));

        // Edge cases
        assertEquals(0, ReadabilityUtils.countSyllables("123")); // Non-alpha
        assertEquals(1, ReadabilityUtils.countSyllables("word123")); // Mixed
    }

    @Test
    public void testCountWords() {
        assertEquals(0, ReadabilityUtils.countWords(null));
        assertEquals(0, ReadabilityUtils.countWords(""));
        assertEquals(1, ReadabilityUtils.countWords("Hello"));
        assertEquals(2, ReadabilityUtils.countWords("Hello world"));
        assertEquals(2, ReadabilityUtils.countWords("Hello   world")); // Multiple spaces
    }

    @Test
    public void testCountSentences() {
        assertEquals(0, ReadabilityUtils.countSentences(null));
        assertEquals(0, ReadabilityUtils.countSentences(""));
        assertEquals(1, ReadabilityUtils.countSentences("Hello world."));
        assertEquals(2, ReadabilityUtils.countSentences("Hello world. How are you?"));
        assertEquals(2, ReadabilityUtils.countSentences("Hello world! I am fine."));
        assertEquals(1, ReadabilityUtils.countSentences("Hello world")); // No punctuation
    }

    @Test
    public void testCalculateFleschKincaidGradeLevel() {
        // "The cat sat on the mat."
        // Words: 6
        // Sentences: 1
        // Syllables: 6 (all 1 syllable)
        // Score = 0.39 * (6/1) + 11.8 * (6/6) - 15.59
        // Score = 2.34 + 11.8 - 15.59 = -1.45 -> clamped to 0
        assertEquals(0.0, ReadabilityUtils.calculateFleschKincaidGradeLevel("The cat sat on the mat."), 0.1);

        // A more complex sentence to get a positive score
        // "The australian platypus is seemingly a hybrid of a mammal and reptilian creature."
        // Words: 13
        // Sentences: 1
        // Syllables:
        // The (1) australian (4) platypus (3) is (1) seemingly (3) a (1) hybrid (2) of (1) a (1) mammal (2) and (1) reptilian (4) creature (2?).
        // approx 26 syllables.
        // Score = 0.39 * (13) + 11.8 * (26/13) - 15.59
        // Score = 5.07 + 23.6 - 15.59 = 13.08

        double score = ReadabilityUtils.calculateFleschKincaidGradeLevel("The australian platypus is seemingly a hybrid of a mammal and reptilian creature.");
        assertTrue("Score should be greater than 0", score > 0);
    }
}
