package com.najmi.oreamnos.cardgen.model

/**
 * Sealed class holding AI-extracted, structured data for each card type.
 * Each variant maps to the JSON structure returned by the corresponding CardPromptManager prompt.
 *
 * All String fields are guaranteed non-null (extractor substitutes placeholders for missing/null values).
 */
sealed class CardData {

    /**
     * Data for the Match Result card.
     * Contains team names, score, key stats, and a Malay key moment summary.
     */
    data class MatchResult(
        val homeTeam: String,
        val awayTeam: String,
        val homeScore: Int,
        val awayScore: Int,
        val competition: String,
        val matchDate: String,
        val homeStats: TeamStats,
        val awayStats: TeamStats,
        val keyMoment: String
    ) : CardData()

    /**
     * Data for the Player Spotlight card.
     * Contains player performance metrics and a Malay description.
     */
    data class PlayerSpotlight(
        val playerName: String,
        val club: String,
        val position: String,
        val rating: Float,
        val goals: Int,
        val assists: Int,
        val keyQuote: String
    ) : CardData()

    /**
     * Data for the Headline / Quote card.
     * Contains an impactful headline, short subtext, and source publication.
     */
    data class HeadlineQuote(
        val headline: String,
        val subtext: String,
        val source: String
    ) : CardData()

    /**
     * Data for the Top 3 Stats card.
     * Always contains exactly 3 [StatItem] entries (extractor pads with placeholders if needed).
     */
    data class TopStats(
        val stats: List<StatItem>
    ) : CardData()
}

/**
 * A single stat row used in [CardData.MatchResult] and [CardData.TopStats].
 */
data class TeamStats(
    val possession: Int,
    val shots: Int,
    val shotsOnTarget: Int
)

/**
 * A single stat row for the Top 3 Stats card.
 */
data class StatItem(
    val label: String,
    val value: String,
    val context: String
)
