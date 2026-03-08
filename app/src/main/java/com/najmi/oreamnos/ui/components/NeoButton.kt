package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Neo-Editorial Button
 * Rectangular, bold text, high contrast.
 * Supports loading state and press animation.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    // Neo-brutalist interaction: Darken on press for immediate visual feedback
    // This creates a "heavy" mechanical feel without relying on soft ripples
    val animatedContainerColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isPressed) containerColor.copy(alpha = 0.8f) else containerColor,
        animationSpec = tween(durationMillis = 50),
        label = "buttonColor"
    )

    // OPTIMIZATION: Remember derived colors to avoid allocation on every recomposition
    val disabledContainerColor = remember(containerColor) { containerColor.copy(alpha = 0.5f) }
    val disabledContentColor = remember(contentColor) { contentColor.copy(alpha = 0.5f) }

    Button(
        onClick = {
            // Lighter haptic feedback for snappy response (Standard click vs Long Press)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onClick()
        },
        // OPTIMIZATION: Use graphicsLayer to avoid recomposition during animation
        modifier = modifier.graphicsLayer {
            scaleX = scaleState.value
            scaleY = scaleState.value
        },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(0.dp), // Sharp corners
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        interactionSource = interactionSource
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "buttonContent"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NeoOutlinedButton(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleState.value
                scaleY = scaleState.value
            }
            .border(
                border = BorderStroke(2.dp, if (enabled) contentColor else contentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(0.dp)
            )
            .background(Color.Transparent, RoundedCornerShape(0.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    if (onLongClick != null) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)
        )
    }
}
