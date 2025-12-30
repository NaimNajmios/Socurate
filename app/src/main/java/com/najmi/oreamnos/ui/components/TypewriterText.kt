package com.najmi.oreamnos.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.isActive

/**
 * Typewriter Text Effect
 * Animates text character by character.
 * Supports both String and AnnotatedString for markdown formatting.
 *
 * OPTIMIZED: Uses frame-based timing instead of delay(1) to prevent excessive recomposition
 * and align updates with the display refresh rate.
 */
@Composable
fun TypewriterText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    onFinished: () -> Unit = {}
) {
    // OPTIMIZATION: Use produceState to manage the animation state efficiently
    // This synchronizes updates with the frame rate, avoiding wasteful recompositions
    // that occur faster than the screen can refresh.
    val displayedText by produceState(initialValue = AnnotatedString(""), key1 = text) {
        // If empty, finish immediately
        if (text.isEmpty()) {
            value = text
            onFinished()
            return@produceState
        }

        val startTime = withFrameNanos { it }
        val textLength = text.length

        // Target speed: ~3 chars per ms (matching original intent of "near instant")
        // 3 chars/ms = 3 chars / 1,000,000 ns = 0.000003 chars/nanosecond
        val charsPerNs = 0.000003

        while (isActive) {
            val now = withFrameNanos { it }
            val elapsedNs = now - startTime

            val count = (elapsedNs * charsPerNs).toInt().coerceAtMost(textLength)

            // Only update value if it changed (optimization)
            if (value.length != count) {
                value = text.subSequence(0, count)
            }

            if (count >= textLength) {
                onFinished()
                break
            }
        }
    }
    
    Text(
        text = displayedText,
        modifier = modifier,
        style = style
    )
}
