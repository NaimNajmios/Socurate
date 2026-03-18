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

    /** Match stats comparison card: side-by-side stats comparison. */
    object MatchStatsComparison : CardTemplate(
        displayName = "Comparison",
        description = "Match Stats Comparison"
    )

    /** Social post card: minimalist X/Twitter/Threads style post. */
    object SocialPost : CardTemplate(
        displayName = "Social",
        description = "Minimalist Social Post"
    )

    /** Rivalry card: head-to-head player/team comparison. */
    object Rivalry : CardTemplate(
        displayName = "Rivalry",
        description = "Head-to-Head Comparison"
    )

    /** League table/standings card. */
    object TableStandings : CardTemplate(
        displayName = "Table",
        description = "League Standings"
    )

    /** Injury report card: squad injury news. */
    object InjuryReport : CardTemplate(
        displayName = "Injury",
        description = "Injury Report"
    )

    /** Contract expiry card: players with expiring contracts. */
    object ContractExpiry : CardTemplate(
        displayName = "Contract",
        description = "Expiring Contracts"
    )

    /** Award nominee card: awards season nominees. */
    object AwardNominee : CardTemplate(
        displayName = "Award",
        description = "Award Nominees"
    )

    companion object {
        /** All templates in display order. Lazy to avoid class init order NPE. */
        val all: List<CardTemplate> by lazy {
            listOf(
                HeadlineQuote, BreakingNews, SocialPost, PlayerSpotlight, TopStats,
                OnThisDay, TransferNews, MatchPreview, StartingXI, DetailedScoreboard,
                MatchStatsComparison, Rivalry, TableStandings, InjuryReport, ContractExpiry,
                AwardNominee
            )
        }
    }
}
