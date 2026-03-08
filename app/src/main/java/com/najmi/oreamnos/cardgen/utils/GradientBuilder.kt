package com.najmi.oreamnos.cardgen.utils

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Builds [Brush] gradients from color pairs for card backgrounds.
 *
 * Uses vertical linear gradients by default, matching the card's top-to-bottom layout.
 */
object GradientBuilder {

    /**
     * Creates a vertical gradient brush from [colors].
     */
    fun vertical(colors: Pair<Color, Color>): Brush =
        Brush.verticalGradient(listOf(colors.first, colors.second))

    /**
     * Creates a diagonal gradient brush from [colors] for a more dynamic look.
     */
    fun diagonal(colors: Pair<Color, Color>): Brush =
        Brush.linearGradient(listOf(colors.first, colors.second))

    /**
     * Creates a radial gradient from [colors], useful for the player spotlight card
     * where the gradient should emanate from the center.
     */
    fun radial(colors: Pair<Color, Color>): Brush =
        Brush.radialGradient(listOf(colors.first, colors.second))

    /**
     * Full dark scrim overlay for gallery images — maximum text legibility.
     * Alpha scales relatively from the provided base opacity.
     */
    fun darkScrim(baseOpacity: Float = 0.6f): Brush =
        Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.45f to Color.Transparent,
            0.65f to Color.Black.copy(alpha = baseOpacity * 0.8f),
            1.0f to Color.Black.copy(alpha = minOf(baseOpacity * 1.5f, 1.0f))
        )

    /**
     * Light scrim — shows more of the image, only darkens at bottom for text.
     * Perfect for NBA-style split layouts where image is on one side.
     */
    fun lightScrim(baseOpacity: Float = 0.6f): Brush =
        Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.6f to Color.Transparent,
            0.8f to Color.Black.copy(alpha = baseOpacity * 0.6f),
            1.0f to Color.Black.copy(alpha = minOf(baseOpacity * 1.1f, 1.0f))
        )

    /**
     * Minimal scrim — barely darkens, best for when image is primary focus.
     */
    fun minimalScrim(baseOpacity: Float = 0.6f): Brush =
        Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.7f to Color.Transparent,
            0.9f to Color.Black.copy(alpha = baseOpacity * 0.4f),
            1.0f to Color.Black.copy(alpha = baseOpacity * 0.75f)
        )

    /**
     * No scrim — image fully visible, no overlay.
     */
    fun noScrim(baseOpacity: Float = 0.6f): Brush =
        Brush.verticalGradient(
            0.0f to Color.Transparent,
            1.0f to Color.Transparent
        )

    /**
     * Horizontal scrim — for split layouts with text on right.
     */
    fun horizontalScrim(baseOpacity: Float = 0.6f): Brush =
        Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.5f to Color.Transparent,
            0.75f to Color.Black.copy(alpha = baseOpacity * 0.8f),
            1.0f to Color.Black.copy(alpha = minOf(baseOpacity * 1.4f, 1.0f))
        )

    /**
     * Reverse horizontal scrim — for split layouts with text on left.
     */
    fun reverseHorizontalScrim(baseOpacity: Float = 0.6f): Brush =
        Brush.horizontalGradient(
            0.0f to Color.Black.copy(alpha = minOf(baseOpacity * 1.4f, 1.0f)),
            0.25f to Color.Black.copy(alpha = baseOpacity * 0.8f),
            0.5f to Color.Transparent,
            1.0f to Color.Transparent
        )

    /**
     * Gets scrim intensity by name and dynamic opacity.
     */
    fun getScrim(type: ScrimType, baseOpacity: Float = 0.6f): Brush = when (type) {
        ScrimType.DARK -> darkScrim(baseOpacity)
        ScrimType.LIGHT -> lightScrim(baseOpacity)
        ScrimType.MINIMAL -> minimalScrim(baseOpacity)
        ScrimType.NONE -> noScrim(baseOpacity)
        ScrimType.HORIZONTAL -> horizontalScrim(baseOpacity)
        ScrimType.REVERSE_HORIZONTAL -> reverseHorizontalScrim(baseOpacity)
    }

    /**
     * Scrim intensity options.
     */
    enum class ScrimType {
        DARK,      // Full coverage for text readability
        LIGHT,     // Light coverage, shows more image
        MINIMAL,   // Barely darkens
        NONE,      // No scrim at all
        HORIZONTAL,    // For split-left layouts
        REVERSE_HORIZONTAL  // For split-right layouts
    }
}
