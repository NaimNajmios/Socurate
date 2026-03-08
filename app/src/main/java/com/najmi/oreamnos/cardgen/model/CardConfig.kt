package com.najmi.oreamnos.cardgen.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.najmi.oreamnos.cardgen.utils.GradientBuilder

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
 * Image position options - determines how images are displayed in the card.
 * Follows NBA-style sports graphics visual hierarchy.
 */
enum class ImagePosition(
    val displayName: String,
    val description: String
) {
    /** Full background image with dark scrim for text readability */
    BACKGROUND("Full Background", "Image fills entire card with overlay"),

    /** Image takes 60% on left, text on right - ideal for quote cards */
    SPLIT_LEFT("Split Left", "Image left (60%), text right"),

    /** Image takes 60% on right, text on left - ideal for match highlights */
    SPLIT_RIGHT("Split Right", "Image right (60%), text left"),

    /** Full-bleed image with text overlaid at bottom 30% - player spotlight style */
    OVERLAY_TOP("Overlay Top", "Full image, text at bottom"),

    /** Transparent PNG cutout with text beside - NBA player card style */
    CUTOUT("Cutout Mode", "Transparent player image overlay"),

    /** Minimal background - image subtle in background, text prominent */
    MINIMAL("Minimal", "Subtle background, prominent text")
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
    SQUARE(1080, 1080, "Square (1:1)"),
    /** 1080 × 1350 px — Instagram feed portrait. */
    PORTRAIT(1080, 1350, "Portrait (4:5)"),
    /** 1080 × 1920 px — Instagram / Facebook Stories. */
    STORY(1080, 1920, "Story (9:16)")
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
    val template: CardTemplate = CardTemplate.DetailedScoreboard,
    val backgroundType: BackgroundType = BackgroundType.GRADIENT,
    /** Start and end colors for the gradient background. */
    val colorPair: Pair<Color, Color> = Pair(Color(0xFF1A237E), Color(0xFF0D47A1)),
    val exportSize: ExportSize = ExportSize.SQUARE,
    /** Set when the user picks a photo from the gallery. */
    val backgroundBitmap: Bitmap? = null,
    /** Set when the user picks a preset. */
    val presetBackground: PresetBackground? = null,
    /** How the image is positioned in the card - determines layout */
    val imagePosition: ImagePosition = ImagePosition.BACKGROUND,
    /** Image opacity (0.0 to 1.0) - useful for MINIMAL and OVERLAY_TOP modes */
    val imageOpacity: Float = 1.0f,
    /** Whether to show scrim overlay on images */
    val showScrim: Boolean = true,
    /** Scrim intensity type - controls how much the image is darkened for text legibility */
    val scrimType: GradientBuilder.ScrimType = GradientBuilder.ScrimType.DARK,
    /** For CUTOUT mode - secondary bitmap for player cutout (transparent PNG) */
    val cutoutBitmap: Bitmap? = null,
    /** Font size multiplier (1.0 = default, 0.8 = smaller, 1.2 = larger) */
    val fontSizeMultiplier: Float = 1.0f,
    /** Master opacity for the dark scrim (0.0 to 1.0, default 0.6) */
    val overlayOpacity: Float = 0.6f,
    /** Optional explicit font family choice (overriding default AppTypography) */
    val primaryFontFamilyName: String? = null,
    /** Optional accent color to override default text colorations */
    val accentColor: Color? = null,
    /** Stores X,Y offsets for draggable components (key = component ID, value = (x,y)) */
    val elementOffsets: Map<String, Pair<Float, Float>> = emptyMap()
)
