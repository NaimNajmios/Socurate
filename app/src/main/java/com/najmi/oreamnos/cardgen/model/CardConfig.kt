package com.najmi.oreamnos.cardgen.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

/**
 * Background type options for the card generator.
 */
enum class BackgroundType {
    /** Auto-generated gradient from team colors. */
    GRADIENT,
    /** User's chosen gallery photo (blurred overlay applied). */
    GALLERY,
    /** Bundled drawable preset (stadium, mesh, grass). */
    PRESET
}

/**
 * Export size options for the card generator.
 */
enum class ExportSize(
    val widthPx: Int,
    val heightPx: Int,
    val label: String
) {
    /** 1080 × 1080 px — Instagram feed / Facebook post. */
    SQUARE(1080, 1080, "Segiempat (1:1)"),
    /** 1080 × 1350 px — Instagram feed portrait. */
    PORTRAIT(1080, 1350, "Potret (4:5)"),
    /** 1080 × 1920 px — Instagram / Facebook Stories. */
    STORY(1080, 1920, "Stori (9:16)")
}

/**
 * Preset background drawable resource IDs bundled in `res/drawable/`.
 */
enum class PresetBackground(val resourceName: String) {
    STADIUM_BLUR("bg_stadium_blur"),
    DARK_MESH("bg_dark_mesh"),
    GRASS_TEXTURE("bg_grass_texture")
}

/**
 * Full configuration for a card currently being generated.
 * Drives both the live preview and the final bitmap export.
 */
data class CardConfig(
    val template: CardTemplate = CardTemplate.MatchResult,
    val backgroundType: BackgroundType = BackgroundType.GRADIENT,
    /** Start and end colors for the gradient background. */
    val colorPair: Pair<Color, Color> = Pair(Color(0xFF1A237E), Color(0xFF0D47A1)),
    val exportSize: ExportSize = ExportSize.SQUARE,
    /** Set when the user picks a photo from the gallery. */
    val backgroundBitmap: Bitmap? = null,
    /** Set when the user picks a preset. */
    val presetBackground: PresetBackground? = null
)
