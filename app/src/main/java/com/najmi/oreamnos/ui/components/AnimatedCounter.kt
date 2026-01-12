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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

/**
 * Animated Integer Counter
 * smoothly transitions between integer values.
 * Uses tabular figures to prevent layout jitter.
 */
@Composable
fun AnimatedIntCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    prefix: String = "",
    suffix: String = ""
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "int_counter"
    )

    Text(
        text = "$prefix$animatedValue$suffix",
        modifier = modifier,
        style = style.copy(
            fontFeatureSettings = "tnum"
        )
    )
}

/**
 * Animated Float Counter
 * smoothly transitions between float values.
 */
@Composable
fun AnimatedFloatCounter(
    value: Float,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    decimals: Int = 1,
    prefix: String = "",
    suffix: String = ""
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "float_counter"
    )

    Text(
        text = "$prefix${String.format("%." + decimals + "f", animatedValue)}$suffix",
        modifier = modifier,
        style = style.copy(
            fontFeatureSettings = "tnum"
        )
    )
}
