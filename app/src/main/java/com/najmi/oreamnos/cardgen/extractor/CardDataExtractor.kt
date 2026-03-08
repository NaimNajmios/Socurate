package com.najmi.oreamnos.cardgen.extractor

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import java.io.StringReader
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.CardTemplate
import com.najmi.oreamnos.cardgen.model.LineupPlayer
import com.najmi.oreamnos.cardgen.model.StatItem

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
        private const val UNKNOWN = ""
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
    suspend fun extract(template: CardTemplate, articleText: String, isRefresh: Boolean = false): Result<CardData> {
        return try {
            val curator = CuratorFactory.create(context)

            // Build the combined prompt — system instruction is baked into the user message
            // because IContentCurator.curatePost() doesn't expose a separate system param.
            val fullPrompt = "${CardPromptManager.systemPrompt()}\n\n${CardPromptManager.buildPrompt(template, articleText, isRefresh)}"

            // generateRaw() bypasses the Malay social-media prompt injections in curatePost
            val rawResponse = curator.generateRaw(prompt = fullPrompt)

            Log.d(TAG, "Raw AI response for $template:\n$rawResponse")

            val cleanJson = stripFences(rawResponse)
            val cardData = parseJson(template, cleanJson)
            Result.success(cardData)

        } catch (e: com.najmi.oreamnos.exceptions.RateLimitException) {
            // Re-throw — handled by the existing rate limit dialog in the UI layer
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract card data for $template", e)
            Result.failure(Exception("Could not extract card data: ${e.message}", e))
        }
    }

    /**
     * Strips markdown code fences from the AI response.
     *
     * Strategy:
     * 1. If a ```json ... ``` or ``` ... ``` fence is found, extract its content.
     * 2. Otherwise, extract the first complete  { … }  block by bracket-matching.
     *    This handles AI responses that prepend prose ("Sure, here is the JSON:").
     * 3. If neither strategy finds valid JSON, return the trimmed raw string.
     *
     * Exposed as internal for unit testing.
     */
    internal fun stripFences(raw: String): String {
        val trimmed = raw.trim()

        // 1. Try code fence extraction
        val fenceMatch = FENCE_PATTERN.find(trimmed)
        if (fenceMatch != null) return fenceMatch.groupValues[1].trim()

        // 2. Find first '{' and match to its closing '}'
        val startIdx = trimmed.indexOf('{')
        if (startIdx >= 0) {
            var depth = 0
            var endIdx = -1
            for (i in startIdx..trimmed.lastIndex) {
                when (trimmed[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) { endIdx = i; break }
                    }
                }
            }
            if (endIdx > startIdx) return trimmed.substring(startIdx, endIdx + 1)
        }

        return trimmed
    }

    /**
     * Parses a clean JSON string into the appropriate [CardData] subclass.
     * Uses a lenient [JsonReader] so minor AI quirks (trailing commas, etc.) don't throw.
     * If the parsed element is not a JsonObject (e.g. AI returned a string or array),
     * falls back to bracket-scanning the raw string for the first { ... } block.
     * Missing fields are replaced with safe placeholder values.
     * Exposed as internal for unit testing.
     *
     * @throws Exception if no JSON object can be found at all.
     */
    internal fun parseJson(template: CardTemplate, json: String): CardData {
        val obj: JsonObject = parseToObject(json)
            ?: throw Exception("The AI response did not contain a JSON object. Try again.")

        return when (template) {

            CardTemplate.PlayerSpotlight -> parsePlayerSpotlight(obj)
            CardTemplate.HeadlineQuote -> parseHeadlineQuote(obj)
            CardTemplate.TopStats -> parseTopStats(obj)
            CardTemplate.TransferNews -> parseTransferNews(obj)
            CardTemplate.BreakingNews -> parseBreakingNews(obj)
            CardTemplate.MatchPreview -> parseMatchPreview(obj)
            CardTemplate.DetailedScoreboard -> parseDetailedScoreboard(obj)
            CardTemplate.OnThisDay -> parseOnThisDay(obj)
            CardTemplate.StartingXI -> parseStartingXI(obj)
        }
    }

    /**
     * Attempts to parse [json] into a [JsonObject].
     *
     * 1. Direct lenient parse — if the result is already a JsonObject, return it.
     * 2. If the result is a non-object (string/array), scan [json] for the first
     *    balanced { ... } block and retry parsing that substring.
     * 3. Return null if no object can be extracted.
     */
    private fun parseToObject(json: String): JsonObject? {
        // Attempt 1: direct lenient parse
        val element = try {
            val reader = JsonReader(StringReader(json)).also { it.isLenient = true }
            JsonParser.parseReader(reader)
        } catch (e: Exception) {
            Log.w(TAG, "Lenient parse failed, trying bracket scan: ${e.message}")
            null
        }

        if (element != null && element.isJsonObject) return element.asJsonObject

        // Attempt 2: bracket-scan the raw string for a {...} block
        val startIdx = json.indexOf('{')
        if (startIdx >= 0) {
            var depth = 0
            var endIdx = -1
            for (i in startIdx..json.lastIndex) {
                when (json[i]) {
                    '{' -> depth++
                    '}' -> { depth--; if (depth == 0) { endIdx = i; break } }
                }
            }
            if (endIdx > startIdx) {
                val candidate = json.substring(startIdx, endIdx + 1)
                return try {
                    val reader = JsonReader(StringReader(candidate)).also { it.isLenient = true }
                    val parsed = JsonParser.parseReader(reader)
                    if (parsed.isJsonObject) parsed.asJsonObject else null
                } catch (e: Exception) {
                    Log.w(TAG, "Bracket-scan parse also failed: ${e.message}")
                    null
                }
            }
        }

        return null
    }

    // ──────────────────────────────────────────────────────────────
    // Per-template parsers
    // ──────────────────────────────────────────────────────────────

    private fun parsePlayerSpotlight(obj: JsonObject): CardData.PlayerSpotlight {
        return CardData.PlayerSpotlight(
            playerName = obj.optString("playerName", UNKNOWN),
            club = obj.optString("club", UNKNOWN),
            position = obj.optString("position", UNKNOWN),
            rating = obj.optFloat("rating", 0f),
            goals = obj.optInt("goals", 0),
            assists = obj.optInt("assists", 0),
            minutesPlayed = obj.optInt("minutesPlayed", 0),
            keyAction = obj.optString("keyAction", UNKNOWN),
            keyQuote = obj.optString("keyQuote", UNKNOWN)
        )
    }

    private fun parseHeadlineQuote(obj: JsonObject): CardData.HeadlineQuote {
        return CardData.HeadlineQuote(
            headline = obj.optString("headline", UNKNOWN),
            subtext = obj.optString("subtext", ""),
            quoteAuthor = obj.optString("quoteAuthor", "")
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
                        label = item.optString("label", "Stat ${i + 1}"),
                        value = item.optString("value", ZERO_STR),
                        context = item.optString("context", "")
                    )
                )
            }
        }
        // Pad to exactly 3 items if AI returned fewer
        while (items.size < 3) {
            items.add(StatItem(label = "Stat ${items.size + 1}", value = ZERO_STR, context = ""))
        }

        return CardData.TopStats(
            matchContext = obj.optString("matchContext", ""),
            stats = items
        )
    }

    private fun parseTransferNews(obj: JsonObject): CardData.TransferNews {
        return CardData.TransferNews(
            playerName = obj.optString("playerName", UNKNOWN),
            action = obj.optString("action", UNKNOWN),
            fromTeam = obj.optString("fromTeam", UNKNOWN),
            toTeam = obj.optString("toTeam", UNKNOWN),
            fee = obj.optString("fee", UNKNOWN),
            contractLength = obj.optString("contractLength", ""),
            transferType = obj.optString("transferType", ""),
            quote = obj.optString("quote", UNKNOWN)
        )
    }

    private fun parseBreakingNews(obj: JsonObject): CardData.BreakingNews {
        return CardData.BreakingNews(
            label = obj.optString("label", "🚨 BREAKING"),
            headline = obj.optString("headline", UNKNOWN),
            subtext = obj.optString("subtext", ""),
            impactRating = obj.optInt("impactRating", 3),
            relatedTeams = obj.optString("relatedTeams", "")
        )
    }

    private fun parseMatchPreview(obj: JsonObject): CardData.MatchPreview {
        return CardData.MatchPreview(
            competition = obj.optString("competition", UNKNOWN),
            homeTeam = obj.optString("homeTeam", UNKNOWN),
            awayTeam = obj.optString("awayTeam", UNKNOWN),
            homeForm = obj.optString("homeForm", ""),
            awayForm = obj.optString("awayForm", ""),
            matchTime = obj.optString("matchTime", UNKNOWN),
            stadium = obj.optString("stadium", UNKNOWN)
        )
    }

    private fun parseDetailedScoreboard(obj: JsonObject): CardData.DetailedScoreboard {
        return CardData.DetailedScoreboard(
            homeTeam = obj.optString("homeTeam", UNKNOWN),
            awayTeam = obj.optString("awayTeam", UNKNOWN),
            homeScore = obj.optInt("homeScore", 0),
            awayScore = obj.optInt("awayScore", 0),
            homeScorers = obj.optString("homeScorers", ""),
            awayScorers = obj.optString("awayScorers", ""),
            possession = obj.optString("possession", ""),
            shotsOnTarget = obj.optString("shotsOnTarget", ""),
            competition = obj.optString("competition", ""),
            matchStatus = obj.optString("matchStatus", UNKNOWN)
        )
    }

    private fun parseOnThisDay(obj: JsonObject): CardData.OnThisDay {
        val statsArray = try { obj.getAsJsonArray("keyStats") } catch (e: Exception) { null }
        val items = mutableListOf<StatItem>()
        if (statsArray != null) {
            for (i in 0 until statsArray.size()) {
                val item = statsArray[i]?.asJsonObject
                if (item != null) {
                    items.add(
                        StatItem(
                            label = item.optString("label", "Stat ${i + 1}"),
                            value = item.optString("value", ZERO_STR),
                            context = item.optString("context", "")
                        )
                    )
                }
            }
        }
        return CardData.OnThisDay(
            dateLabel = obj.optString("dateLabel", "ON THIS DAY"),
            yearsAgo = obj.optInt("yearsAgo", 0),
            competition = obj.optString("competition", ""),
            headline = obj.optString("headline", UNKNOWN),
            keyStats = items
        )
    }

    private fun parseStartingXI(obj: JsonObject): CardData.StartingXI {
        val startersArray = try { obj.getAsJsonArray("starters") } catch (e: Exception) { null }
        val subsArray = try { obj.getAsJsonArray("subs") } catch (e: Exception) { null }

        val starters = mutableListOf<LineupPlayer>()
        if (startersArray != null) {
            for (i in 0 until startersArray.size()) {
                val item = startersArray[i]?.asJsonObject
                if (item != null) {
                    starters.add(LineupPlayer(
                        number = item.optString("number", ""),
                        name = item.optString("name", UNKNOWN)
                    ))
                }
            }
        }

        val subs = mutableListOf<LineupPlayer>()
        if (subsArray != null) {
            for (i in 0 until subsArray.size()) {
                val item = subsArray[i]?.asJsonObject
                if (item != null) {
                    subs.add(LineupPlayer(
                        number = item.optString("number", ""),
                        name = item.optString("name", UNKNOWN)
                    ))
                }
            }
        }

        return CardData.StartingXI(
            teamName = obj.optString("teamName", UNKNOWN),
            formation = obj.optString("formation", ""),
            starters = starters,
            subs = subs,
            manager = obj.optString("manager", ""),
            averageAge = obj.optString("averageAge", ""),
            keyAbsences = obj.optString("keyAbsences", "")
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
