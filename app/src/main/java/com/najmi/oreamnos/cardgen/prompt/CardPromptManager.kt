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
        "CRITICAL RULE 2: ALWAYS use these accepted English football terms instead of making up stiff direct translations in Bahasa Malaysia. Do NOT translate: " +
        "'Clean Sheet', 'Offside', 'Hat-trick', 'Tackle', 'Assist', 'Playmaker', 'Derby', 'Comeback', 'Winger', 'Striker', 'Midfielder', 'Defender', 'Full-back', 'Center-back', 'Goalkeeper', 'Free-kick', 'Penalty', 'Corner Kicks', 'VAR', 'Counter-attack', 'Pressing', 'Cross', 'Header', 'Nutmeg', 'Dribble', 'Volley', 'Bicycle Kick', 'Man of the Match', 'Golden Boot', 'Pitch', 'Box-to-box', 'Sweeper', 'Target Man', 'False Nine', 'High Press', 'Through Ball', 'Overhead Kick'. " +
        "CRITICAL RULE 3: If a specific piece of information (e.g., stats, dates, fees) is NOT explicitly mentioned in the text, you MUST return an empty string \"\" or 0 for numeric fields. Do NOT guess, infer, or provide placeholders like 'N/A', '-', or '—'."

    fun buildPrompt(template: CardTemplate, articleText: String, isRefresh: Boolean = false): String {
        val schema = when (template) {

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
        
        val cacheBuster = if (isRefresh) {
            "\n\n[SYSTEM NOTE: This is a REFRESH instruction. The user was unhappy with the previous extraction. Please generate slightly different wording, alter phrasing creatively, and ensure you catch any fields you missed previously. Timestamp: ${System.currentTimeMillis()}]"
        } else ""

        return "$schema\n\nARTICLE:\n$articleText$cacheBuster\n\nRespond with ONLY the JSON object, starting with {"
    }

    // ──────────────────────────────────────────────────────────────
    // Schema prompts — no "Bahasa Melayu" instruction, ends with
    // a forcing anchor so the model begins its reply with {
    // ──────────────────────────────────────────────────────────────

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
          "minutesPlayed": 90,
          "keyAction": "Satu frasa pendek (maks 3 patah perkataan, e.g. Wira Hat-Trick)",
          "keyQuote": "Satu ayat menerangkan prestasi pemain tersebut (maks 100 aksara)"
        }
    """.trimIndent()

    private fun headlineQuoteSchema(): String = """
        Extract the single most impactful headline or quote from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "headline": "Tajuk utama atau petikan paling penting (maks 120 aksara)",
          "subtext": "Satu perenggan sokongan ringkas (maks 60 aksara)",
          "quoteAuthor": "Nama penutur (biarkan kosong jika bukan petikan)"
        }
    """.trimIndent()

    private fun topStatsSchema(): String = """
        Extract the 3 most interesting statistics from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "matchContext": "Perlawanan atau kejohanan yang berkaitan (e.g. lwn Liverpool - Liga Perdana)",
          "stats": [
            { "label": "Nama stat (maks 30 aksara)", "value": "Nilai nombor", "context": "Konteks ringkas (maks 50 aksara)" },
            { "label": "Nama stat (maks 30 aksara)", "value": "Nilai nombor", "context": "Konteks ringkas (maks 50 aksara)" },
            { "label": "Nama stat (maks 30 aksara)", "value": "Nilai nombor", "context": "Konteks ringkas (maks 50 aksara)" }
          ]
        }
    """.trimIndent()

    private fun transferNewsSchema(): String = """
        Extract the transfer news or rumors from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "playerName": "Full Name",
          "action": "Status (MUST BE ONE OF: SAH, DIPINJAM, PINDAHAN SELESAI, KHABAR ANGIN)",
          "fromTeam": "Pasukan Asal",
          "toTeam": "Pasukan Baru",
          "fee": "Yuran Perpindahan",
          "contractLength": "Tempoh Kontrak (e.g. 5 Tahun)",
          "transferType": "Jenis (e.g. Tetap, Pinjaman, Percuma)",
          "quote": "Satu petikan ringkas dari pemain, ejen, atau kelab (maks 100 aksara)"
        }
    """.trimIndent()

    private fun breakingNewsSchema(): String = """
        Extract the breaking or urgent news from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "label": "Label Berita (e.g. 🚨 TERKINI, RASMI, EKSKLUSIF)",
          "headline": "Tajuk berita utama (maks 100 aksara)",
          "subtext": "Satu atau dua ayat menerangkan konteks (maks 150 aksara)",
          "impactRating": 5,
          "relatedTeams": "Pasukan yang terjejas (e.g. Man Utd, Arsenal)"
        }
    """.trimIndent()

    private fun matchPreviewSchema(): String = """
        Extract the match preview details for an upcoming game from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "competition": "Nama Liga/Pertandingan",
          "homeTeam": "Nama Pasukan Tuan Rumah",
          "awayTeam": "Nama Pasukan Pelawat",
          "homeForm": "Rekod 5 perlawanan tuan rumah (e.g. M-S-M-K-M)",
          "awayForm": "Rekod 5 perlawanan pelawat (e.g. K-K-S-M-M)",
          "matchTime": "Tarikh dan masa",
          "stadium": "Nama Stadium"
        }
    """.trimIndent()

    private fun detailedScoreboardSchema(): String = """
        Extract the detailed match result from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "homeTeam": "Team Name",
          "awayTeam": "Team Name",
          "homeScore": 0,
          "awayScore": 0,
          "homeScorers": "Senarai penjaring gol tuan rumah (e.g. Rashford 12', 45')",
          "awayScorers": "Senarai penjaring gol pelawat (e.g. Saka 80')",
          "possession": "Penguasaan bola (e.g. 55% - 45%)",
          "shotsOnTarget": "Percubaan tepat (e.g. 6 - 2)",
          "competition": "Nama Liga atau Kejohanan",
          "matchStatus": "Status Tamat (e.g. MASA PENUH, MASA TAMBAHAN, PENALTI)"
        }
    """.trimIndent()

    private fun onThisDaySchema(): String = """
        Extract historical or 'on this day' information from the football article below.
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
        {
          "dateLabel": "Tarikh peristiwa (e.g. 📅 15 Mei)",
          "yearsAgo": 10,
          "competition": "Nama Liga atau Kejohanan",
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
        Return ONLY a JSON object with this exact structure (fill in real values, write descriptions in Bahasa Malaysia but use natural English football terminology where appropriate):
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
          "manager": "Nama Pengurus",
          "averageAge": "Purata Umur (e.g. 25.4 thn)",
          "keyAbsences": "Pemain cedera/digantung, dipisahkan dengan koma"
        }
    """.trimIndent()
}
