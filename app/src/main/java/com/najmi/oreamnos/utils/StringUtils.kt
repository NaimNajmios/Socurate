package com.najmi.oreamnos.utils

import java.util.regex.Pattern

/**
 * Utility object for string manipulation, particularly emoji handling.
 */
object StringUtils {

    // Comprehensive emoji regex pattern that matches most emojis
    private const val EMOJI_PATTERN = "[" +
            "\u00a9\u00ae" + // © ®
            "\u2000-\u3300" + // Various symbols
            "\ud83c\ud000-\ud83c\udfff" + // Enclosed chars, flags, etc.
            "\ud83d\ud000-\ud83d\udfff" + // Emoticons, misc
            "\ud83e\ud000-\ud83e\udfff" + // Extended-A, chess, etc.
            "\u200d" + // ZWJ (zero-width joiner for compound emojis)
            "\ufe0f" + // Variation selector
            "]+"

    // (?m) enables multiline mode where ^ matches start of line
    private val LEADING_EMOJI_PATTERN: Pattern = Pattern.compile("(?m)^($EMOJI_PATTERN)+\\s*")

    /**
     * Strips ALL emojis from the text (anywhere in the text, not just leading).
     */
    @JvmStatic
    fun stripAllEmojis(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace(Regex(EMOJI_PATTERN), "").trim()
    }

    /**
     * Strips leading emojis and whitespace from the beginning of each line/paragraph.
     */
    @JvmStatic
    fun stripLeadingEmojis(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return LEADING_EMOJI_PATTERN.matcher(text).replaceAll("")
    }
}
