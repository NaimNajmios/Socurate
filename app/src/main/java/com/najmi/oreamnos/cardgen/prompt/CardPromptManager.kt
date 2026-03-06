package com.najmi.oreamnos.cardgen.prompt

import com.najmi.oreamnos.cardgen.model.CardTemplate

/**
 * Builds AI prompts for each card template type.
 *
 * Each prompt instructs the AI to return ONLY a JSON object — no markdown fences,
 * no explanation. The [CardDataExtractor] is responsible for stripping any
 * fence characters that sneak through and parsing the result.
 */
object CardPromptManager {

    /**
     * Returns the system instruction common to all card extraction prompts.
     * Keeps the AI focused on pure JSON output.
     */
    fun systemPrompt(): String =
        "You are a football data extractor. Your ONLY task is to extract structured data " +
        "from the article and return it as a valid JSON object. " +
        "Return NOTHING else — no markdown, no code fences, no explanation, no preamble. " +
        "If a field cannot be determined from the article, use a sensible placeholder string."

    /**
     * Returns the full extraction prompt for the given [template] and [articleText].
     */
    fun buildPrompt(template: CardTemplate, articleText: String): String {
        val schema = when (template) {
            CardTemplate.MatchResult -> matchResultSchema()
            CardTemplate.PlayerSpotlight -> playerSpotlightSchema()
            CardTemplate.HeadlineQuote -> headlineQuoteSchema()
            CardTemplate.TopStats -> topStatsSchema()
        }
        return "$schema\n\nARTICLE TEXT:\n$articleText"
    }

    // ──────────────────────────────────────────────────────────────
    // Schema prompts
    // ──────────────────────────────────────────────────────────────

    private fun matchResultSchema(): String = """
        Extract match result data from this football article and return ONLY this JSON structure:
        {
          "homeTeam": "string",
          "awayTeam": "string",
          "homeScore": 0,
          "awayScore": 0,
          "competition": "string",
          "matchDate": "string",
          "homeStats": {
            "possession": 50,
            "shots": 0,
            "shotsOnTarget": 0
          },
          "awayStats": {
            "possession": 50,
            "shots": 0,
            "shotsOnTarget": 0
          },
          "keyMoment": "string max 80 chars in Bahasa Melayu"
        }
    """.trimIndent()

    private fun playerSpotlightSchema(): String = """
        Extract player highlight data from this football article and return ONLY this JSON structure:
        {
          "playerName": "string",
          "club": "string",
          "position": "string",
          "rating": 7.5,
          "goals": 0,
          "assists": 0,
          "keyQuote": "string max 100 chars in Bahasa Melayu describing the performance"
        }
    """.trimIndent()

    private fun headlineQuoteSchema(): String = """
        Extract the single most impactful headline or quote from this football article
        and return ONLY this JSON structure:
        {
          "headline": "string max 120 chars in Bahasa Melayu",
          "subtext": "string max 60 chars in Bahasa Melayu",
          "source": "string — publication or platform name"
        }
    """.trimIndent()

    private fun topStatsSchema(): String = """
        Extract the 3 most interesting statistics from this football article
        and return ONLY this JSON structure:
        {
          "stats": [
            { "label": "string max 30 chars in Bahasa Melayu", "value": "string max 10 chars", "context": "string max 50 chars in Bahasa Melayu" },
            { "label": "string max 30 chars in Bahasa Melayu", "value": "string max 10 chars", "context": "string max 50 chars in Bahasa Melayu" },
            { "label": "string max 30 chars in Bahasa Melayu", "value": "string max 10 chars", "context": "string max 50 chars in Bahasa Melayu" }
          ]
        }
    """.trimIndent()
}
