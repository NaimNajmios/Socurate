package com.najmi.oreamnos.utils;

import java.util.regex.Pattern;

public class StringUtils {

    // Comprehensive emoji regex pattern that matches most emojis
    // This covers: emoticons, symbols, dingbats, pictographs, transport, flags,
    // and emojis composed with ZWJ (zero-width joiner) and variation selectors
    private static final String EMOJI_PATTERN = "[" +
            "\u00a9\u00ae" + // © ®
            "\u2000-\u3300" + // Various symbols
            "\ud83c\ud000-\ud83c\udfff" + // Enclosed chars, flags, etc.
            "\ud83d\ud000-\ud83d\udfff" + // Emoticons, misc
            "\ud83e\ud000-\ud83e\udfff" + // Extended-A, chess, etc.
            "\u200d" + // ZWJ (zero-width joiner for compound emojis)
            "\ufe0f" + // Variation selector
            "]+";

    private static final Pattern LEADING_EMOJI_PATTERN = Pattern.compile("^(" + EMOJI_PATTERN + ")+\\s*");

    /**
     * Strips ALL emojis from the text (anywhere in the text, not just leading).
     * This is a comprehensive removal of all emoji characters.
     */
    public static String stripAllEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        // Remove all emojis from the entire text
        return text.replaceAll(EMOJI_PATTERN, "").trim();
    }

    /**
     * Strips leading emojis and whitespace from the beginning of each
     * line/paragraph.
     * Uses a robust regex to handle most emoji types including compound emojis.
     */
    public static String stripLeadingEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Replace leading emoji and whitespace using the pattern matcher
            line = LEADING_EMOJI_PATTERN.matcher(line).replaceFirst("");
            sb.append(line);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
