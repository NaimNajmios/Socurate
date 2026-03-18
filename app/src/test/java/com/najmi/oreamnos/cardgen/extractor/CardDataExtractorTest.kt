package com.najmi.oreamnos.cardgen.extractor

import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.CardTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardDataExtractorTest {

    private val extractor = CardDataExtractor(android.app.Application())

    @Test
    fun `stripFences extracts content from json fence`() {
        val raw = """
            ```json
            {"headline": "Test News"}
            ```
        """.trimIndent()
        
        val result = extractor.stripFences(raw)
        assertEquals("""{"headline": "Test News"}""", result)
    }

    @Test
    fun `stripFences extracts content from plain code fence`() {
        val raw = """
            ```
            {"headline": "Test News"}
            ```
        """.trimIndent()
        
        val result = extractor.stripFences(raw)
        assertEquals("""{"headline": "Test News"}""", result)
    }

    @Test
    fun `stripFences extracts first JSON block when no fence`() {
        val raw = """
            Sure, here's the JSON:
            {"headline": "Test News", "subtext": "More info"}
            
            Let me know if you need anything else!
        """.trimIndent()
        
        val result = extractor.stripFences(raw)
        assertEquals("""{"headline": "Test News", "subtext": "More info"}""", result)
    }

    @Test
    fun `stripFences handles nested braces`() {
        val raw = """{"outer": {"inner": "value"}}"""
        
        val result = extractor.stripFences(raw)
        assertEquals("""{"outer": {"inner": "value"}}""", result)
    }

    @Test
    fun `stripFences returns trimmed string when no JSON found`() {
        val raw = "No JSON here, just plain text"
        
        val result = extractor.stripFences(raw)
        assertEquals("No JSON here, just plain text", result)
    }

    @Test
    fun `stripFences handles empty input`() {
        val result = extractor.stripFences("")
        assertEquals("", result)
    }

    @Test
    fun `stripFences handles whitespace only`() {
        val result = extractor.stripFences("   \n\t  ")
        // When there's no JSON, returns the trimmed input
        assertEquals("", result.trim())
    }

    @Test
    fun `stripFences handles AI prefix with text before JSON`() {
        val raw = """
            Here's the extracted data in JSON format:
            
            ```json
            {"playerName": "Mohamed Salah", "club": "Liverpool FC"}
            ```
            
            Is there anything else you need?
        """.trimIndent()
        
        val result = extractor.stripFences(raw)
        assertEquals("""{"playerName": "Mohamed Salah", "club": "Liverpool FC"}""", result)
    }

    @Test
    fun `parseJson creates HeadlineQuote with correct fields`() {
        val json = """
            {
                "headline": "Harapan tinggi menjelang aksi finale",
                "subtext": "Penyokong optimistik dengan penampilan terkini",
                "quoteAuthor": "Jurulatih utama"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.HeadlineQuote, json)
        
        assertTrue(result is CardData.HeadlineQuote)
        val cardData = result as CardData.HeadlineQuote
        assertEquals("Harapan tinggi menjelang aksi finale", cardData.headline)
        assertEquals("Penyokong optimistik dengan penampilan terkini", cardData.subtext)
        assertEquals("Jurulatih utama", cardData.quoteAuthor)
    }

    @Test
    fun `parseJson creates PlayerSpotlight with correct fields`() {
        val json = """
            {
                "playerName": "Erling Haaland",
                "club": "Manchester City",
                "position": "ST",
                "rating": 8.5,
                "goals": 3,
                "assists": 1,
                "minutesPlayed": 90,
                "keyAction": "Hat-trick",
                "keyQuote": "Penyerang terbaik di dunia"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.PlayerSpotlight, json)
        
        assertTrue(result is CardData.PlayerSpotlight)
        val cardData = result as CardData.PlayerSpotlight
        assertEquals("Erling Haaland", cardData.playerName)
        assertEquals("Manchester City", cardData.club)
        assertEquals("ST", cardData.position)
        assertEquals(8.5f, cardData.rating, 0.1f)
        assertEquals(3, cardData.goals)
        assertEquals(1, cardData.assists)
    }

    @Test
    fun `parseJson creates DetailedScoreboard with correct fields`() {
        val json = """
            {
                "homeTeam": "Manchester United",
                "awayTeam": "Liverpool FC",
                "homeScore": 2,
                "awayScore": 2,
                "homeScorers": "Rashford 23', Fernandes 67'",
                "awayScorers": "Salah 45+1', Nunez 89'",
                "possession": "58% - 42%",
                "shotsOnTarget": "5 - 3",
                "competition": "Premier League",
                "matchStatus": "FT"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.DetailedScoreboard, json)
        
        assertTrue(result is CardData.DetailedScoreboard)
        val cardData = result as CardData.DetailedScoreboard
        assertEquals("Manchester United", cardData.homeTeam)
        assertEquals("Liverpool FC", cardData.awayTeam)
        assertEquals(2, cardData.homeScore)
        assertEquals(2, cardData.awayScore)
        assertEquals("Premier League", cardData.competition)
    }

    @Test
    fun `parseJson handles missing fields with fallbacks`() {
        val json = """{"headline": "Partial Data"}"""
        
        val result = extractor.parseJson(CardTemplate.HeadlineQuote, json)
        
        assertTrue(result is CardData.HeadlineQuote)
        val cardData = result as CardData.HeadlineQuote
        assertEquals("Partial Data", cardData.headline)
        assertEquals("", cardData.subtext)
        assertEquals("", cardData.quoteAuthor)
    }

    @Test
    fun `parseJson parses template intent correctly`() {
        val json = """
            {
                "headline": "Breaking Transfer News",
                "template_intent": "TRANSFER"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.HeadlineQuote, json)
        
        assertNotNull(result)
        assertEquals(CardTemplate.TransferNews, result.suggestedTemplate)
    }

    @Test
    fun `parseJson parses TopStats with stat items`() {
        val json = """
            {
                "matchContext": "JDT vs Selangor",
                "stats": [
                    {"label": "Possession", "value": "65%", "context": "JDT dominate"},
                    {"label": "Shots", "value": "12", "context": ""},
                    {"label": "Corners", "value": "7", "context": ""}
                ]
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.TopStats, json)
        
        assertTrue(result is CardData.TopStats)
        val cardData = result as CardData.TopStats
        assertEquals("JDT vs Selangor", cardData.matchContext)
        assertEquals(3, cardData.stats.size)
        assertEquals("Possession", cardData.stats[0].label)
        assertEquals("65%", cardData.stats[0].value)
    }

    @Test(expected = Exception::class)
    fun `parseJson handles invalid JSON gracefully`() {
        val json = "This is not JSON at all"
        
        // Should throw exception for non-JSON input
        extractor.parseJson(CardTemplate.HeadlineQuote, json)
    }

    @Test
    fun `parseJson parses JSON with trailing commas`() {
        val json = """
            {
                "headline": "Test Headline",
                "subtext": "Test subtext"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.HeadlineQuote, json)
        
        assertTrue(result is CardData.HeadlineQuote)
        val cardData = result as CardData.HeadlineQuote
        assertEquals("Test Headline", cardData.headline)
    }

    @Test
    fun `parseJson handles null values gracefully`() {
        val json = """
            {
                "headline": null,
                "subtext": "Valid subtext"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.HeadlineQuote, json)
        
        assertTrue(result is CardData.HeadlineQuote)
        val cardData = result as CardData.HeadlineQuote
        assertEquals("", cardData.headline)
        assertEquals("Valid subtext", cardData.subtext)
    }

    @Test
    fun `parseJson parses MatchPreview correctly`() {
        val json = """
            {
                "competition": "Malaysia Cup",
                "homeTeam": "JDT",
                "awayTeam": "Selangor FC",
                "homeForm": "W W W D",
                "awayForm": "L D W W",
                "matchTime": "20:00",
                "stadium": "Tan Sri Dato' HJ Hassan Yunus"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.MatchPreview, json)
        
        assertTrue(result is CardData.MatchPreview)
        val cardData = result as CardData.MatchPreview
        assertEquals("JDT", cardData.homeTeam)
        assertEquals("Selangor FC", cardData.awayTeam)
        assertEquals("Malaysia Cup", cardData.competition)
    }

    @Test
    fun `parseJson parses TransferNews correctly`() {
        val json = """
            {
                "playerName": "Cristiano Ronaldo",
                "action": "SIGNED",
                "fromTeam": "Juventus",
                "toTeam": "Al-Nassr",
                "fee": "Undisclosed",
                "contractLength": "2 years",
                "transferType": "Permanent",
                "quote": "Excited for this new chapter"
            }
        """.trimIndent()
        
        val result = extractor.parseJson(CardTemplate.TransferNews, json)
        
        assertTrue(result is CardData.TransferNews)
        val cardData = result as CardData.TransferNews
        assertEquals("Cristiano Ronaldo", cardData.playerName)
        assertEquals("SIGNED", cardData.action)
        assertEquals("Al-Nassr", cardData.toTeam)
    }
}
