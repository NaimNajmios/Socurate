package com.najmi.oreamnos.cardgen.model

/**
 * Sealed class representing the four available card template types.
 * Each subclass corresponds to a distinct AI extraction prompt and canvas layout.
 */
sealed class CardTemplate(
    val displayName: String,
    val description: String
) {
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

    /** Transfer news card: player signings, loans, rumors. */
    object TransferNews : CardTemplate(
        displayName = "Transfer",
        description = "Transfers & Rumors"
    )

    /** Breaking news card: major announcements and flash news. */
    object BreakingNews : CardTemplate(
        displayName = "Breaking",
        description = "Urgent / Breaking News"
    )

    /** Match preview card: upcoming fixture details. */
    object MatchPreview : CardTemplate(
        displayName = "Preview",
        description = "Upcoming Fixture"
    )

    /** Detailed scoreboard card: scores with goalscorers. */
    object DetailedScoreboard : CardTemplate(
        displayName = "Full Time",
        description = "Detailed Scoreboard"
    )

    /** Historical card: on this day anniversaries. */
    object OnThisDay : CardTemplate(
        displayName = "History",
        description = "On This Day Anniversary"
    )

    /** Lineup card: starting XI and substitutes. */
    object StartingXI : CardTemplate(
        displayName = "Lineup",
        description = "Starting XI / Pitch"
    )

    companion object {
        /** All templates in display order. Lazy to avoid class init order NPE. */
        val all: List<CardTemplate> by lazy {
            listOf(
                PlayerSpotlight, HeadlineQuote, TopStats,
                TransferNews, BreakingNews, MatchPreview, DetailedScoreboard, OnThisDay, StartingXI
            )
        }
    }
}
