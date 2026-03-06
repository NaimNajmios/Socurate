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
     * Dark scrim overlay for gallery images — ensures text legibility.
     * Alpha 0.45 matches the implementation plan specification.
     */
    val darkScrim: Brush =
        Brush.verticalGradient(
            listOf(
                Color.Black.copy(alpha = 0.1f),
                Color.Black.copy(alpha = 0.45f),
                Color.Black.copy(alpha = 0.65f)
            )
        )
}
