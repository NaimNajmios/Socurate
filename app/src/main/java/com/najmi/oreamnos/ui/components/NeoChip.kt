package com.najmi.oreamnos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Neo-Editorial Chip
 * Rectangular, outlined or filled, sharp corners.
 * Supports optional long-press gesture for editing.
 * Supports custom colors for visual differentiation (e.g., custom pills).
 * Features smooth color transitions and tactile press feedback.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NeoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    unselectedBorderColor: Color? = null,
    unselectedTextColor: Color? = null
) {
    // Animation Specs
    val colorSpec = tween<Color>(durationMillis = 200)
    val scaleSpec = tween<Float>(durationMillis = 100)

    // Colors with smooth transition
    val targetBackgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val animatedBackgroundColor by animateColorAsState(targetBackgroundColor, colorSpec, label = "bg")

    val targetContentColor = if (selected)
        MaterialTheme.colorScheme.onPrimary 
    else 
        unselectedTextColor ?: MaterialTheme.colorScheme.onSurface
    val animatedContentColor by animateColorAsState(targetContentColor, colorSpec, label = "content")

    val targetBorderColor = if (selected)
        MaterialTheme.colorScheme.primary 
    else 
        unselectedBorderColor ?: MaterialTheme.colorScheme.outline
    val animatedBorderColor by animateColorAsState(targetBorderColor, colorSpec, label = "border")

    // Interaction State for Tactile Feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = scaleSpec,
        label = "scale"
    )

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        modifier = modifier
            // OPTIMIZATION: Use graphicsLayer to avoid recomposition during animation
            .graphicsLayer {
                scaleX = scaleState.value
                scaleY = scaleState.value
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // We handle visual feedback via scale/color
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = onLongClick?.let {
                    {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        it()
                    }
                }
            ),
        shape = RoundedCornerShape(0.dp),
        color = animatedBackgroundColor,
        border = BorderStroke(2.dp, animatedBorderColor),
        shadowElevation = 0.dp
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = animatedContentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
