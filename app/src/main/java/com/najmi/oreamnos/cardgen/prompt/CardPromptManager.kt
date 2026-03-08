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
        "CRITICAL RULE 1: Translate ALL extracted text values into Malaysian Malay (Bahasa Malaysia) EXCEPT for proper nouns like player names, club names, or tournament acronyms. " +
        "CRITICAL RULE 2: If a specific piece of information (e.g., stats, dates, source, fees) is NOT explicitly mentioned in the text, you MUST return an empty string \"\" or 0 for numeric fields. Do NOT guess, infer, or provide placeholders like 'N/A', '-', or '—'."

    fun buildPrompt(template: CardTemplate, articleText: String): String {
        val schema = when (template) {
            CardTemplate.MatchResult -> matchResultSchema()
            CardTemplate.PlayerSpotlight -> playerSpotlightSchema()
            CardTemplate.HeadlineQuote -> headlineQuoteSchema()
            CardTemplate.TopStats -> topStatsSchema()
            CardTemplate.TransferNews -> transferNewsSchema()
            CardTemplate.BreakingNews -> breakingNewsSchema()
            CardTemplate.MatchPreview -> matchPreviewSchema()
            CardTemplate.DetailedScoreboard -> detailedScoreboardSchema()
            CardTemplate.OnThisDay -> onThisDaySchema()
            CardTemplate.StartingXI -> startingXISchema()
        }
        return "$schema\n\nARTICLE:\n$articleText\n\nRespond with ONLY the JSON object, starting with {"
    }

    // ──────────────────────────────────────────────────────────────
    // Schema prompts — no "Bahasa Melayu" instruction, ends with
    // a forcing anchor so the model begins its reply with {
    // ──────────────────────────────────────────────────────────────

    private fun matchResultSchema(): String = """
        Extract match result data from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "homeTeam": "Team Name",
          "awayTeam": "Team Name",
          "homeScore": 0,
          "awayScore": 0,
          "competition": "Competition Name",
          "matchDate": "DD Mon YYYY",
          "homeStats": { "possession": 50, "shots": 0, "shotsOnTarget": 0 },
          "awayStats": { "possession": 50, "shots": 0, "shotsOnTarget": 0 },
          "keyMoment": "Satu ayat menerangkan detik penting perlawanan (maks 80 aksara)"
        }
    """.trimIndent()

    private fun playerSpotlightSchema(): String = """
        Extract the standout player's data from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "playerName": "Full Name",
          "club": "Club Name",
          "position": "Posisi Pemain (Bahasa Melayu)",
          "rating": 7.5,
          "goals": 0,
          "assists": 0,
          "keyQuote": "Satu ayat menerangkan prestasi pemain tersebut (maks 100 aksara)"
        }
    """.trimIndent()

    private fun headlineQuoteSchema(): String = """
        Extract the single most impactful headline or quote from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "headline": "Tajuk utama atau petikan paling penting (maks 120 aksara)",
          "subtext": "Satu perenggan sokongan ringkas (maks 60 aksara)",
          "source": "Nama julukan sumber / majalah"
        }
    """.trimIndent()

    private fun topStatsSchema(): String = """
        Extract the 3 most interesting statistics from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "stats": [
            { "label": "Nama stat (maks 30 aksara)", "value": "Nilai nombor", "context": "Konteks ringkas (maks 50 aksara)" },
            { "label": "Nama stat (maks 30 aksara)", "value": "Nilai nombor", "context": "Konteks ringkas (maks 50 aksara)" },
            { "label": "Nama stat (maks 30 aksara)", "value": "Nilai nombor", "context": "Konteks ringkas (maks 50 aksara)" }
          ]
        }
    """.trimIndent()

    private fun transferNewsSchema(): String = """
        Extract the transfer news or rumors from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "playerName": "Full Name",
          "action": "Status (e.g. SAH, DIPINJAM, PINDAHAN SELESAI, RUMUR)",
          "fromTeam": "Pasukan Asal",
          "toTeam": "Pasukan Baru",
          "fee": "Yuran Perpindahan / Tempoh Kontrak",
          "quote": "Satu petikan ringkas dari pemain, ejen, atau kelab (maks 100 aksara)"
        }
    """.trimIndent()

    private fun breakingNewsSchema(): String = """
        Extract the breaking or urgent news from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "label": "Label Berita (e.g. 🚨 TERKINI, RASMI, EKSKLUSIF)",
          "headline": "Tajuk berita utama (maks 100 aksara)",
          "subtext": "Satu atau dua ayat menerangkan konteks (maks 150 aksara)"
        }
    """.trimIndent()

    private fun matchPreviewSchema(): String = """
        Extract the match preview details for an upcoming game from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "competition": "Nama Liga/Pertandingan",
          "homeTeam": "Nama Pasukan Tuan Rumah",
          "awayTeam": "Nama Pasukan Pelawat",
          "matchTime": "Tarikh dan masa",
          "stadium": "Nama Stadium"
        }
    """.trimIndent()

    private fun detailedScoreboardSchema(): String = """
        Extract the detailed match result from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "homeTeam": "Team Name",
          "awayTeam": "Team Name",
          "homeScore": 0,
          "awayScore": 0,
          "homeScorers": "Senarai penjaring gol tuan rumah (e.g. Rashford 12', 45')",
          "awayScorers": "Senarai penjaring gol pelawat (e.g. Saka 80')",
          "matchStatus": "Status Tamat (e.g. MASA PENUH, MASA TAMBAHAN, PENALTI)"
        }
    """.trimIndent()

    private fun onThisDaySchema(): String = """
        Extract historical or 'on this day' information from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "dateLabel": "Tarikh peristiwa (e.g. 📅 HARI INI DALAM SEJARAH: 2012)",
          "headline": "Satu ayat menerangkan apa yang berlaku (maks 100 aksara)",
          "keyStats": [
            { "label": "Stat 1", "value": "Nilai nombor", "context": "Konteks 1" },
            { "label": "Stat 2", "value": "Nilai nombor", "context": "Konteks 2" },
            { "label": "Stat 3", "value": "Nilai nombor", "context": "Konteks 3" }
          ]
        }
    """.trimIndent()

    private fun startingXISchema(): String = """
        Extract the starting lineup or predicted lineup from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia):
        {
          "teamName": "Nama Pasukan",
          "formation": "Formasi (e.g. 4-3-3)",
          "starters": [
            { "number": "No Jersi (jika ada, kalau tidak kosongkan)", "name": "Nama Pemain 1" },
            { "number": "No Jersi", "name": "Nama Pemain 2" }
          ],
          "subs": [
            { "number": "No Jersi", "name": "Nama Pemain Simpanan 1" },
            { "number": "No Jersi", "name": "Nama Pemain Simpanan 2" }
          ],
          "manager": "Nama Pengurus"
        }
    """.trimIndent()
}
