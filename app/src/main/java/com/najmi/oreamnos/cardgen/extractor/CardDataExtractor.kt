package com.najmi.oreamnos.cardgen.extractor

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.CardTemplate
import com.najmi.oreamnos.cardgen.model.StatItem
import com.najmi.oreamnos.cardgen.model.TeamStats
import com.najmi.oreamnos.cardgen.prompt.CardPromptManager
import com.najmi.oreamnos.curator.CuratorFactory

/**
 * Extracts structured [CardData] from article text by calling the configured AI provider
 * via [CuratorFactory] and parsing the returned JSON.
 *
 * Error strategy:
 * - JSON parse failures → [Result.failure] with descriptive exception
 * - Missing/null fields → placeholder strings so the card still renders
 * - Rate limit exceptions → propagated as-is for the existing rate limit dialog to handle
 */
class CardDataExtractor(private val context: Context) {

    companion object {
        private const val TAG = "CardDataExtractor"

        // Fallback placeholder values for missing fields
        private const val UNKNOWN = "—"
        private const val ZERO_STR = "0"

        // Regex to strip ```json ... ``` or ``` ... ``` fences from AI responses
        private val FENCE_PATTERN = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```")
    }

    private val gson = Gson()

    /**
     * Extracts [CardData] for the given [template] from [articleText].
     *
     * This is a suspend function — call it from a coroutine (e.g. from the ViewModel).
     *
     * @return [Result.success] with [CardData] on success, [Result.failure] on parse error.
     *         Rate limit [com.najmi.oreamnos.exceptions.RateLimitException] is re-thrown
     *         so the existing rate limit dialog handles it.
     */
    suspend fun extract(template: CardTemplate, articleText: String): Result<CardData> {
        return try {
            val curator = CuratorFactory.create(context)

            // Build the combined prompt — system instruction is baked into the user message
            // because IContentCurator.curatePost() doesn't expose a separate system param.
            val fullPrompt = "${CardPromptManager.systemPrompt()}\n\n${CardPromptManager.buildPrompt(template, articleText)}"

            // curatePost with no source / no structure keeps the response as raw JSON
            val rawResponse = curator.curatePost(
                inputText = fullPrompt,
                includeSource = false,
                keepStructure = true
            )

            Log.d(TAG, "Raw AI response for $template:\n$rawResponse")

            val cleanJson = stripFences(rawResponse)
            val cardData = parseJson(template, cleanJson)
            Result.success(cardData)

        } catch (e: com.najmi.oreamnos.exceptions.RateLimitException) {
            // Re-throw — handled by the existing rate limit dialog in the UI layer
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract card data for $template", e)
            Result.failure(Exception("Tidak dapat mengekstrak data kad: ${e.message}", e))
        }
    }

    /**
     * Strips markdown code fences from the AI response.
     * Exposed as internal for unit testing.
     */
    internal fun stripFences(raw: String): String {
        val match = FENCE_PATTERN.find(raw.trim())
        return if (match != null) match.groupValues[1].trim() else raw.trim()
    }

    /**
     * Parses a clean JSON string into the appropriate [CardData] subclass.
     * Missing fields are replaced with safe placeholder values.
     * Exposed as internal for unit testing.
     *
     * @throws Exception if the JSON is structurally malformed (not parseable at all).
     */
    internal fun parseJson(template: CardTemplate, json: String): CardData {
        val obj: JsonObject = try {
            JsonParser.parseString(json).asJsonObject
        } catch (e: Exception) {
            throw Exception("JSON tidak sah: ${e.message}")
        }

        return when (template) {
            CardTemplate.MatchResult -> parseMatchResult(obj)
            CardTemplate.PlayerSpotlight -> parsePlayerSpotlight(obj)
            CardTemplate.HeadlineQuote -> parseHeadlineQuote(obj)
            CardTemplate.TopStats -> parseTopStats(obj)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Per-template parsers
    // ──────────────────────────────────────────────────────────────

    private fun parseMatchResult(obj: JsonObject): CardData.MatchResult {
        return CardData.MatchResult(
            homeTeam = obj.optString("homeTeam", UNKNOWN),
            awayTeam = obj.optString("awayTeam", UNKNOWN),
            homeScore = obj.optInt("homeScore", 0),
            awayScore = obj.optInt("awayScore", 0),
            competition = obj.optString("competition", UNKNOWN),
            matchDate = obj.optString("matchDate", UNKNOWN),
            homeStats = parseTeamStats(obj.optObject("homeStats")),
            awayStats = parseTeamStats(obj.optObject("awayStats")),
            keyMoment = obj.optString("keyMoment", UNKNOWN)
        )
    }

    private fun parsePlayerSpotlight(obj: JsonObject): CardData.PlayerSpotlight {
        return CardData.PlayerSpotlight(
            playerName = obj.optString("playerName", UNKNOWN),
            club = obj.optString("club", UNKNOWN),
            position = obj.optString("position", UNKNOWN),
            rating = obj.optFloat("rating", 0f),
            goals = obj.optInt("goals", 0),
            assists = obj.optInt("assists", 0),
            keyQuote = obj.optString("keyQuote", UNKNOWN)
        )
    }

    private fun parseHeadlineQuote(obj: JsonObject): CardData.HeadlineQuote {
        return CardData.HeadlineQuote(
            headline = obj.optString("headline", UNKNOWN),
            subtext = obj.optString("subtext", ""),
            source = obj.optString("source", UNKNOWN)
        )
    }

    private fun parseTopStats(obj: JsonObject): CardData.TopStats {
        val statsArray = try {
            obj.getAsJsonArray("stats")
        } catch (e: Exception) {
            null
        }

        val items = mutableListOf<StatItem>()
        if (statsArray != null) {
            for (i in 0 until minOf(statsArray.size(), 3)) {
                val item = statsArray[i]?.asJsonObject
                items.add(
                    StatItem(
                        label = item.optString("label", "Statistik ${i + 1}"),
                        value = item.optString("value", ZERO_STR),
                        context = item.optString("context", "")
                    )
                )
            }
        }
        // Pad to exactly 3 items if AI returned fewer
        while (items.size < 3) {
            items.add(StatItem(label = "Statistik ${items.size + 1}", value = ZERO_STR, context = ""))
        }

        return CardData.TopStats(stats = items)
    }

    private fun parseTeamStats(obj: JsonObject?): TeamStats {
        if (obj == null) return TeamStats(possession = 50, shots = 0, shotsOnTarget = 0)
        return TeamStats(
            possession = obj.optInt("possession", 50),
            shots = obj.optInt("shots", 0),
            shotsOnTarget = obj.optInt("shotsOnTarget", 0)
        )
    }

    // ──────────────────────────────────────────────────────────────
    // JsonObject extension helpers (safe field access)
    // ──────────────────────────────────────────────────────────────

    private fun JsonObject?.optString(key: String, fallback: String): String {
        if (this == null) return fallback
        return try {
            val element = get(key)
            if (element == null || element.isJsonNull) fallback else element.asString.ifBlank { fallback }
        } catch (e: Exception) { fallback }
    }

    private fun JsonObject?.optInt(key: String, fallback: Int): Int {
        if (this == null) return fallback
        return try {
            val element = get(key)
            if (element == null || element.isJsonNull) fallback else element.asInt
        } catch (e: Exception) { fallback }
    }

    private fun JsonObject?.optFloat(key: String, fallback: Float): Float {
        if (this == null) return fallback
        return try {
            val element = get(key)
            if (element == null || element.isJsonNull) fallback else element.asFloat
        } catch (e: Exception) { fallback }
    }

    private fun JsonObject.optObject(key: String): JsonObject? {
        return try {
            val element = get(key)
            if (element == null || element.isJsonNull) null else element.asJsonObject
        } catch (e: Exception) { null }
    }
}
