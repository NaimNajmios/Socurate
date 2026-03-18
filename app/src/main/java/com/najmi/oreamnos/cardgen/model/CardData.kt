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
        val nationality: String = "",
        val appearances: Int = 0,
        val cleanSheets: Int = 0,
        val passes: Int = 0,
        val tackles: Int = 0,
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
        val authorTitle: String = "",
        val category: String = "",
        val relatedTeams: String = "",
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
        val action: String,
        val fromTeam: String,
        val toTeam: String,
        val fee: String,
        val contractLength: String,
        val transferType: String,
        val quote: String,
        val feeCategory: String = "",
        val medicalCompleted: Boolean = false,
        val workPermit: Boolean = false,
        val agentName: String = "",
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
        val homeForm: String,
        val awayForm: String,
        val matchTime: String,
        val stadium: String,
        val referee: String = "",
        val tvChannel: String = "",
        val kickoffTime: String = "",
        val weather: String = "",
        val capacity: String = "",
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
        val homeScorers: String,
        val awayScorers: String,
        val possession: String,
        val shotsOnTarget: String,
        val competition: String,
        val matchStatus: String,
        val corners: String = "",
        val fouls: String = "",
        val yellowCards: String = "",
        val redCards: String = "",
        val attendance: String = "",
        val referee: String = "",
        val penaltyShootout: String = "",
        val assistProviders: String = "",
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
        val venue: String = "",
        val attendance: String = "",
        val result: String = "",
        val significance: String = "",
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
        val captain: String = "",
        val viceCaptain: String = "",
        val tactics: String = "",
        val injuredPlayers: String = "",
        val suspendedPlayers: String = "",
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)
    /**
     * Data for the Match Stats Comparison card.
     */
    data class MatchStatsComparison(
        val homeTeam: String,
        val awayTeam: String,
        val stats: List<ComparisonStat>,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the minimalist Social Post card.
     */
    data class SocialPost(
        val handle: String,
        val name: String,
        val content: String,
        val timestamp: String,
        val metrics: String,
        val verified: Boolean = false,
        val followers: String = "",
        val shares: String = "",
        val bookmarks: String = "",
        val mediaType: String = "",
        val isEdited: Boolean = false,
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Rivalry / Head-to-Head comparison card.
     */
    data class Rivalry(
        val player1Name: String,
        val player2Name: String,
        val matchContext: String,
        val player1Stats: List<StatItem>,
        val player2Stats: List<StatItem>,
        val headToHead: String,
        val verdict: String,
        val compareType: String = "",
        val totalMatches: String = "",
        val draws: String = "",
        val player1Trophies: String = "",
        val player2Trophies: String = "",
        val predictionConfidence: String = "",
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the League Table / Standings card.
     */
    data class TableStandings(
        val leagueName: String,
        val matchday: String,
        val standings: List<TableRow>,
        val highlightedTeam: String,
        val promotionZone: Int = 4,
        val relegationZone: Int = 18,
        val gamesInHand: String = "",
        val pointsBehindLeader: String = "",
        val topScorer: String = "",
        val topAssists: String = "",
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Injury Report card.
     */
    data class InjuryReport(
        val teamName: String,
        val reportDate: String,
        val injuries: List<InjuryItem>,
        val doubtfits: List<InjuryItem>,
        val returns: List<InjuryItem>,
        val nextMatch: String = "",
        val recoveryPercentage: String = "",
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Contract Expiry card.
     */
    data class ContractExpiry(
        val teamName: String,
        val seasonYear: String,
        val expiringPlayers: List<ContractPlayer>,
        val renewals: List<ContractPlayer>,
        val wage: String = "",
        val askingPrice: String = "",
        val interestLevel: String = "",
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)

    /**
     * Data for the Award Nominee card.
     */
    data class AwardNominee(
        val awardName: String,
        val category: String,
        val nominees: List<NomineeItem>,
        val ceremonyDate: String,
        val currentFavorite: String,
        val votingDeadline: String = "",
        val votingMethod: String = "",
        val totalNominees: Int = 0,
        val venue: String = "",
        val host: String = "",
        override val suggestedTemplate: CardTemplate? = null
    ) : CardData(suggestedTemplate)
}

/**
 * A single stat row for the Match Stats Comparison card.
 */
data class ComparisonStat(
    val label: String,
    val homeValue: String,
    val awayValue: String
)

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

/**
 * A single row for the League Table card.
 */
data class TableRow(
    val position: Int,
    val teamName: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val points: Int,
    val form: String
)

/**
 * A single injury entry for the Injury Report card.
 */
data class InjuryItem(
    val playerName: String,
    val injury: String,
    val status: String,
    val position: String,
    val recoveryPercentage: String = "",
    val isLongTerm: Boolean = false,
    val surgeryRequired: Boolean = false
)

/**
 * A single contract entry for the Contract Expiry card.
 */
data class ContractPlayer(
    val playerName: String,
    val position: String,
    val expiresIn: String,
    val marketValue: String,
    val status: String,
    val wage: String = "",
    val askingPrice: String = "",
    val interestLevel: String = "",
    val negotiationProgress: String = "",
    val previousClub: String = ""
)

/**
 * A single nominee entry for the Award Nominee card.
 */
data class NomineeItem(
    val playerName: String,
    val club: String,
    val achievement: String,
    val odds: String,
    val isFavorite: Boolean = false,
    val previousWinner: Boolean = false,
    val votes: String = ""
)
