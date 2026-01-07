package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
 *
 * Update: Added fluid selection animation with pop effect and check icon.
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

    // Selection Pop Animation
    // We want a distinct "pop" when selecting, separate from the press animation
    val selectionScale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            selectionScale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(100)
            )
            selectionScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        } else {
            // Reset scale immediately if deselected to prevent getting stuck
            selectionScale.snapTo(1f)
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = scaleSpec,
        label = "press_scale"
    )

    // Combine scales
    val finalScale = pressScale * selectionScale.value

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        modifier = modifier
            // OPTIMIZATION: Use graphicsLayer to avoid recomposition during animation
            .graphicsLayer {
                scaleX = finalScale
                scaleY = finalScale
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
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = animatedContentColor
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }

            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = animatedContentColor
            )
        }
    }
}
