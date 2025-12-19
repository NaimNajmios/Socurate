package com.najmi.oreamnos.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * Strips leading emojis and whitespace from the beginning of each
     * line/paragraph.
     * Uses a robust regex to handle surrogate pairs and common symbol ranges.
     */
    public static String stripLeadingEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        // Regex to match emojis at the start of lines/paragraphs
        // Matches surrogate pairs (most emojis) and common symbol ranges
        String emojiRegex = "^([\\uD800-\\uDBFF][\\uDC00-\\uDFFF]|[\\u2600-\\u27BF])+\\s*";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Replace leading emoji and whitespace
            line = line.replaceAll(emojiRegex, "");
            sb.append(line);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Extracts leading emojis from the first line of text.
     * Returns the emoji(s) with trailing space, or empty string if none found.
     */
    public static String extractLeadingEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        // Get the first line only
        String firstLine = text.split("\n")[0];

        // Regex to match emojis at the start
        String emojiRegex = "^([\\uD800-\\uDBFF][\\uDC00-\\uDFFF]|[\\u2600-\\u27BF])+\\s*";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emojiRegex);
        java.util.regex.Matcher matcher = pattern.matcher(firstLine);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * Strips leading emojis from all paragraphs EXCEPT the first one.
     * This ensures only the first paragraph has an emoji, avoiding multiple emojis.
     */
    public static String stripEmojisExceptFirst(String text) {
        if (text == null || text.isEmpty())
            return "";

        String emojiRegex = "^([\\uD800-\\uDBFF][\\uDC00-\\uDFFF]|[\\u2600-\\u27BF])+\\s*";

        // Split by double newline (paragraphs)
        String[] paragraphs = text.split("\n\n");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paragraphs.length; i++) {
            String para = paragraphs[i];
            if (i > 0) {
                // Strip emojis from 2nd paragraph onwards
                para = para.replaceAll(emojiRegex, "");
            }
            sb.append(para);
            if (i < paragraphs.length - 1) {
                sb.append("\n\n");
            }
        }

        return sb.toString();
    }
}
