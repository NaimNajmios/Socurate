package com.najmi.oreamnos.cardgen.model

/**
 * Sealed class holding AI-extracted, structured data for each card type.
 * Each variant maps to the JSON structure returned by the corresponding CardPromptManager prompt.
 *
 * All String fields are guaranteed non-null (extractor substitutes placeholders for missing/null values).
 */
sealed class CardData(open val suggestedTemplate: CardTemplate? = null) {

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
        val minutesPlayed: Int,
        val keyAction: String,
        val keyQuote: String,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Headline / Quote card.
     * Contains an impactful headline, short subtext, and source publication.
     */
    data class HeadlineQuote(
        val headline: String,
        val subtext: String,
        val quoteAuthor: String,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Top 3 Stats card.
     * Always contains exactly 3 [StatItem] entries (extractor pads with placeholders if needed).
     */
    data class TopStats(
        val matchContext: String,
        val stats: List<StatItem>,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Transfer News / Here We Go card.
     */
    data class TransferNews(
        val playerName: String,
        val action: String, // e.g. "SIGNED", "LOANED", "AGREEMENT REACHED"
        val fromTeam: String,
        val toTeam: String,
        val fee: String,
        val contractLength: String,
        val transferType: String,
        val quote: String,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Breaking News / Flash card.
     */
    data class BreakingNews(
        val label: String, // e.g. "🚨 BREAKING", "OFFICIAL"
        val headline: String,
        val subtext: String,
        val impactRating: Int, // 1 to 5
        val relatedTeams: String,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Upcoming Match Preview card.
     */
    data class MatchPreview(
        val competition: String,
        val homeTeam: String,
        val awayTeam: String,
        val homeForm: String, // e.g. "W-W-D-L-W"
        val awayForm: String,
        val matchTime: String,
        val stadium: String,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Detailed Scoreboard / Full-Time card.
     */
    data class DetailedScoreboard(
        val homeTeam: String,
        val awayTeam: String,
        val homeScore: Int,
        val awayScore: Int,
        val homeScorers: String, // Comma separated list of scorers & minutes
        val awayScorers: String,
        val possession: String, // e.g. "55% - 45%"
        val shotsOnTarget: String, // e.g. "6 - 2"
        val competition: String,
        val matchStatus: String, // e.g. "FULL TIME", "AET"
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the On This Day / Historical card.
     */
    data class OnThisDay(
        val dateLabel: String,
        val yearsAgo: Int,
        val competition: String,
        val headline: String,
        val keyStats: List<StatItem>,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Starting XI / Lineup card.
     */
    data class StartingXI(
        val teamName: String,
        val formation: String,
        val starters: List<LineupPlayer>,
        val subs: List<LineupPlayer>,
        val manager: String,
        val averageAge: String,
        val keyAbsences: String,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)
}

/**
 * A single stat row for the Top 3 Stats card.
 */
data class StatItem(
    val label: String,
    val value: String,
    val context: String
)

/**
 * A single player entry for the StartingXI card.
 */
data class LineupPlayer(
    val number: String,
    val name: String
)
