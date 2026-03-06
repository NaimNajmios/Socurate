package com.najmi.oreamnos.cardgen.model

/**
 * Sealed class representing the four available card template types.
 * Each subclass corresponds to a distinct AI extraction prompt and canvas layout.
 */
sealed class CardTemplate(
    val displayName: String,
    val description: String
) {
    /** Match result card: score, teams, possession, shots stats. */
    object MatchResult : CardTemplate(
        displayName = "Result",
        description = "Match Score & Stats"
    )

    /** Player spotlight card: player name, rating, goals, assists. */
    object PlayerSpotlight : CardTemplate(
        displayName = "Player",
        description = "Player Performance Spotlight"
    )

    /** Headline / quote card: key quote or headline from article. */
    object HeadlineQuote : CardTemplate(
        displayName = "Headline",
        description = "Key Quote or Headline"
    )

    /** Top 3 stats card: the three most interesting statistics. */
    object TopStats : CardTemplate(
        displayName = "Stats",
        description = "Top 3 Statistics"
    )

    /** NBA style card: A short, punchy quote with key player stats attached. */
    object NbaStyleQuote : CardTemplate(
        displayName = "NBA Style",
        description = "Short Quote & Core Stats"
    )

    companion object {
        /** All templates in display order. Lazy to avoid class init order NPE. */
        val all: List<CardTemplate> by lazy {
            listOf(MatchResult, PlayerSpotlight, HeadlineQuote, TopStats, NbaStyleQuote)
        }
    }
}
