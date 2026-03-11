package com.najmi.oreamnos.cardgen.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette

/**
 * Maps Malaysian football club names to a color pair (start, end) for gradient backgrounds.
 * Matching is case-insensitive and does partial string matching.
 *
 * Falls back to a deep navy pair if the team name is not recognised.
 */
object ColorExtractor {

    // Club name keyword → Pair(gradientStart, gradientEnd)
    private val clubColorMap: Map<String, Pair<Color, Color>> = mapOf(
        // Johor Darul Ta'zim — Yellow/Blue
        "jdt"            to Pair(Color(0xFFFFD100), Color(0xFF003087)),
        "johor"          to Pair(Color(0xFFFFD100), Color(0xFF003087)),
        // Selangor — Red/Yellow/Black
        "selangor"       to Pair(Color(0xFFD21034), Color(0xFF1A0A00)),
        // Pahang — Black/Yellow
        "pahang"         to Pair(Color(0xFF1A1A1A), Color(0xFFFFC200)),
        // Kedah — Red
        "kedah"          to Pair(Color(0xFFCC0000), Color(0xFF7A0000)),
        // Perak — Silver/Blue
        "perak"          to Pair(Color(0xFF4A90D9), Color(0xFFC0C0C0)),
        // Terengganu FC
        "terengganu"     to Pair(Color(0xFF006994), Color(0xFF002D55)),
        // Sabah FA
        "sabah"          to Pair(Color(0xFF003580), Color(0xFF001F4D)),
        // PDRM FC
        "pdrm"           to Pair(Color(0xFF003087), Color(0xFF001A52)),
        // Kuala Lumpur City FC
        "kuala lumpur"   to Pair(Color(0xFF8B0000), Color(0xFF3A0000)),
        "klcity"         to Pair(Color(0xFF8B0000), Color(0xFF3A0000)),
        // Sri Pahang
        "sri pahang"     to Pair(Color(0xFF2D2D2D), Color(0xFFB8860B)),
        // Negeri Sembilan
        "negeri sembilan" to Pair(Color(0xFFFFD700), Color(0xFF8B0000)),
        // Malaysia national team
        "malaysia"       to Pair(Color(0xFFCC0001), Color(0xFF003087)),
        "harimau"        to Pair(Color(0xFFCC0001), Color(0xFF003087)),
    )

    // Fallback gradient if team not found
    private val fallback = Pair(Color(0xFF1A237E), Color(0xFF0D47A1)) // Deep Navy

    /**
     * Returns a color pair for the given team name.
     * Performs case-insensitive partial matching.
     */
    fun getColorsForTeam(teamName: String): Pair<Color, Color> {
        val lower = teamName.lowercase().trim()
        for ((keyword, colors) in clubColorMap) {
            if (lower.contains(keyword)) return colors
        }
        return fallback
    }

    /**
     * Extracts a blended color pair from two team names (home and away).
     * Uses the home team's gradient start and the away team's gradient end.
     */
    fun getMatchColors(homeTeam: String, awayTeam: String): Pair<Color, Color> {
        val homeColors = getColorsForTeam(homeTeam)
        val awayColors = getColorsForTeam(awayTeam)
        return Pair(homeColors.first, awayColors.second)
    }

    /** All six preset color swatches shown in the BackgroundPickerSheet gradient tab. */
    val presetSwatches: List<Triple<String, Color, Color>> = listOf(
        Triple("JDT",         Color(0xFFFFD100), Color(0xFF003087)),
        Triple("Selangor",    Color(0xFFD21034), Color(0xFF1A0A00)),
        Triple("Pahang",      Color(0xFF1A1A1A), Color(0xFFFFC200)),
        Triple("Kedah",       Color(0xFFCC0000), Color(0xFF7A0000)),
        Triple("Perak",       Color(0xFF4A90D9), Color(0xFFC0C0C0)),
        Triple("Malaysia",    Color(0xFFCC0001), Color(0xFF003087)),
    )

    /**
     * Extracts a vibrant color pair from a bitmap using the Palette library.
     */
    fun extractPalette(bitmap: Bitmap): Pair<Color, Color> {
        val palette = Palette.from(bitmap).generate()
        val primary = palette.getVibrantColor(palette.getMutedColor(fallback.first.toArgb()))
        val secondary = palette.getDarkVibrantColor(palette.getDarkMutedColor(fallback.second.toArgb()))
        return Pair(Color(primary), Color(secondary))
    }
}
