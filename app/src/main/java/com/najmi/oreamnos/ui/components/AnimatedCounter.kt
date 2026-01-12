package com.najmi.oreamnos.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Animated Integer Counter
 * Smoothly interpolates between integer values.
 * Uses tabular figures ("tnum") to prevent layout jitter.
 */
@Composable
fun AnimatedIntCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = Color.Unspecified
) {
    val count by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "int_counter"
    )

    Text(
        text = "$count",
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        maxLines = 1
    )
}

/**
 * Animated Float Counter
 * Smoothly interpolates between float values.
 * Formats to 1 decimal place.
 * Uses tabular figures ("tnum") to prevent layout jitter.
 */
@Composable
fun AnimatedFloatCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = Color.Unspecified
) {
    val count by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "float_counter"
    )

    Text(
        text = String.format("%.1f", count),
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        maxLines = 1
    )
}
