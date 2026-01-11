package com.najmi.oreamnos.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReadabilityUtilsTest {

    @Test
    public void testCountWords() {
        assertEquals(0, ReadabilityUtils.countWords(null));
        assertEquals(0, ReadabilityUtils.countWords(""));
        assertEquals(0, ReadabilityUtils.countWords("   "));
        assertEquals(1, ReadabilityUtils.countWords("Hello"));
        assertEquals(2, ReadabilityUtils.countWords("Hello world"));
        assertEquals(2, ReadabilityUtils.countWords("Hello   world")); // Multiple spaces
        assertEquals(3, ReadabilityUtils.countWords("  Hello world  test  ")); // Leading/trailing spaces
    }

    @Test
    public void testCalculateFleschKincaidGradeLevel_NullAndEmpty() {
        assertEquals(0.0, ReadabilityUtils.calculateFleschKincaidGradeLevel(null), 0.01);
        assertEquals(0.0, ReadabilityUtils.calculateFleschKincaidGradeLevel(""), 0.01);
        assertEquals(0.0, ReadabilityUtils.calculateFleschKincaidGradeLevel("   "), 0.01);
    }

    @Test
    public void testCalculateFleschKincaidGradeLevel_SimpleSentence() {
        // "The cat sat on the mat."
        // Words: 6
        // Sentences: 1
        // Syllables: 6 (all 1 syllable)
        // Score = 0.39 * (6/1) + 11.8 * (6/6) - 15.59
        // Score = 2.34 + 11.8 - 15.59 = -1.45 -> clamped to 0
        assertEquals(0.0, ReadabilityUtils.calculateFleschKincaidGradeLevel("The cat sat on the mat."), 0.1);
    }

    @Test
    public void testCalculateFleschKincaidGradeLevel_ComplexSentence() {
        // A more complex sentence should produce a positive score
        double score = ReadabilityUtils.calculateFleschKincaidGradeLevel(
                "The australian platypus is seemingly a hybrid of a mammal and reptilian creature.");
        assertTrue("Score should be greater than 0 for complex sentences", score > 0);
    }

    @Test
    public void testCalculateFleschKincaidGradeLevel_MultipleSentences() {
        // Multiple sentences should be handled correctly
        double score = ReadabilityUtils.calculateFleschKincaidGradeLevel(
                "Hello world. How are you? I am fine!");
        // Should return a valid positive or zero score
        assertTrue("Score should be non-negative", score >= 0);
    }

    @Test
    public void testCalculateFleschKincaidGradeLevel_NoTerminator() {
        // Text without sentence terminator should still work
        double score = ReadabilityUtils.calculateFleschKincaidGradeLevel("Hello world");
        assertTrue("Score should be non-negative", score >= 0);
    }
}
