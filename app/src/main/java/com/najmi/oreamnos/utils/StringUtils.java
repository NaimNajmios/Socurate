package com.najmi.oreamnos.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    // Regex to match emojis at the start of lines/paragraphs
    // Matches surrogate pairs (most emojis) and common symbol ranges
    private static final java.util.regex.Pattern EMOJI_REGEX_PATTERN = java.util.regex.Pattern.compile("^([\\uD800-\\uDBFF][\\uDC00-\\uDFFF]|[\\u2600-\\u27BF])+\\s*");

    /**
     * Strips leading emojis and whitespace from the beginning of each
     * line/paragraph.
     * Uses a robust regex to handle surrogate pairs and common symbol ranges.
     */
    public static String stripLeadingEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Replace leading emoji and whitespace
            line = EMOJI_REGEX_PATTERN.matcher(line).replaceAll("");
            sb.append(line);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
