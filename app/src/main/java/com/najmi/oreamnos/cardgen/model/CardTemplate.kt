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
        displayName = "Keputusan",
        description = "Skor & Statistik Perlawanan"
    )

    /** Player spotlight card: player name, rating, goals, assists. */
    object PlayerSpotlight : CardTemplate(
        displayName = "Pemain",
        description = "Sorotan Prestasi Pemain"
    )

    /** Headline / quote card: key quote or headline from article. */
    object HeadlineQuote : CardTemplate(
        displayName = "Tajuk",
        description = "Petikan atau Tajuk Utama"
    )

    /** Top 3 stats card: the three most interesting statistics. */
    object TopStats : CardTemplate(
        displayName = "Statistik",
        description = "3 Statistik Terbaik"
    )

    companion object {
        /** All templates in display order. Lazy to avoid class init order NPE. */
        val all: List<CardTemplate> by lazy {
            listOf(MatchResult, PlayerSpotlight, HeadlineQuote, TopStats)
        }
    }
}
