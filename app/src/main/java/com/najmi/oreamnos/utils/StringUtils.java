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

    // Bolt Optimization: Use Multiline flag to match start of lines without splitting the string
    // Use [ \t]* instead of \s* to avoid matching newlines
    private static final Pattern LEADING_EMOJI_PATTERN = Pattern.compile("^(" + EMOJI_PATTERN + ")+[ \\t]*", Pattern.MULTILINE);

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
     * <p>
     * Optimized by Bolt: Uses Multiline regex to avoid string splitting and array allocation.
     */
    public static String stripLeadingEmojis(String text) {
        if (text == null || text.isEmpty())
            return "";

        // Bolt Optimization: Replace all matches in one pass using Multiline pattern
        // This avoids split(), array allocation, and StringBuilder operations
        return LEADING_EMOJI_PATTERN.matcher(text).replaceAll("");
    }
}
