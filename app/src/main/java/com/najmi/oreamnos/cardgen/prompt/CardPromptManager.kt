package com.najmi.oreamnos.cardgen.prompt

import com.najmi.oreamnos.cardgen.model.CardTemplate

/**
 * Builds AI prompts for each card template type.
 *
 * Each prompt instructs the AI to return ONLY a JSON object — no markdown fences,
 * no explanation. The [CardDataExtractor] strips stray fences and parses the result.
 */
object CardPromptManager {

    fun systemPrompt(): String =
        "You are a structured data extractor for football (soccer) articles. " +
        "Your ONLY output must be a single valid JSON object. " +
        "Do NOT include any explanation, preamble, markdown, code fences, or text outside the JSON. " +
        "Start your response with { and end it with }. " +
        "Use English for all extracted text values unless the source text includes proper nouns."

    fun buildPrompt(template: CardTemplate, articleText: String): String {
        val schema = when (template) {
            CardTemplate.MatchResult -> matchResultSchema()
            CardTemplate.PlayerSpotlight -> playerSpotlightSchema()
            CardTemplate.HeadlineQuote -> headlineQuoteSchema()
            CardTemplate.TopStats -> topStatsSchema()
        }
        return "$schema\n\nARTICLE:\n$articleText\n\nRespond with ONLY the JSON object, starting with {"
    }

    // ──────────────────────────────────────────────────────────────
    // Schema prompts — no "Bahasa Melayu" instruction, ends with
    // a forcing anchor so the model begins its reply with {
    // ──────────────────────────────────────────────────────────────

    private fun matchResultSchema(): String = """
        Extract match result data from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values):
        {
          "homeTeam": "Team Name",
          "awayTeam": "Team Name",
          "homeScore": 0,
          "awayScore": 0,
          "competition": "Competition Name",
          "matchDate": "DD Mon YYYY",
          "homeStats": { "possession": 50, "shots": 0, "shotsOnTarget": 0 },
          "awayStats": { "possession": 50, "shots": 0, "shotsOnTarget": 0 },
          "keyMoment": "One sentence describing the key moment of the match (max 80 chars)"
        }
    """.trimIndent()

    private fun playerSpotlightSchema(): String = """
        Extract the standout player's data from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values):
        {
          "playerName": "Full Name",
          "club": "Club Name",
          "position": "Position",
          "rating": 7.5,
          "goals": 0,
          "assists": 0,
          "keyQuote": "One sentence describing the player's performance (max 100 chars)"
        }
    """.trimIndent()

    private fun headlineQuoteSchema(): String = """
        Extract the single most impactful headline or quote from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values):
        {
          "headline": "The main headline or quote (max 120 chars)",
          "subtext": "A brief supporting context (max 60 chars)",
          "source": "Publication or platform name"
        }
    """.trimIndent()

    private fun topStatsSchema(): String = """
        Extract the 3 most interesting statistics from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values):
        {
          "stats": [
            { "label": "Stat name (max 30 chars)", "value": "Numeric value", "context": "Brief context (max 50 chars)" },
            { "label": "Stat name (max 30 chars)", "value": "Numeric value", "context": "Brief context (max 50 chars)" },
            { "label": "Stat name (max 30 chars)", "value": "Numeric value", "context": "Brief context (max 50 chars)" }
          ]
        }
    """.trimIndent()
}
