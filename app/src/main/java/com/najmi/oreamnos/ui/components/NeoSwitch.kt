package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Neo-Brutalist Switch Component.
 * Features a rectangular design with sharp corners, high contrast state changes,
 * and tactile haptic feedback.
 *
 * @param checked Whether the switch is checked
 * @param onCheckedChange Callback when the switch is toggled
 * @param modifier Modifier for the layout
 * @param enabled Whether the switch is enabled
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NeoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Animate alignment bias from -1 (left) to 1 (right)
    // Using a spring for a more mechanical/tactile feel
    val alignmentBias by animateFloatAsState(
        targetValue = if (checked) 1f else -1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "switch_bias"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "track_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "border_color"
    )

    // Using Surface color for the thumb when checked ensures high contrast with the Primary icon
    val thumbColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "thumb_color"
    )

    // Icon color is Primary to match the track and provide brand reinforcement
    val iconColor = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .alpha(if (enabled) 1f else 0.5f) // Visual feedback for disabled state
            .toggleable(
                value = checked,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(it)
                },
                role = Role.Switch,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null // Disable ripple for mechanical feel
            )
            .border(2.dp, borderColor, RoundedCornerShape(0.dp))
            .background(trackColor, RoundedCornerShape(0.dp))
            .padding(4.dp), // Inner padding between border and thumb
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(BiasAlignment(alignmentBias, 0f))
                    .size(24.dp) // Thumb size
                    .background(thumbColor, RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                 // Delightful micro-interaction: Show checkmark when active
                 AnimatedContent(
                    targetState = checked,
                    transitionSpec = {
                        scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) with
                        scaleOut(animationSpec = tween(100))
                    },
                    label = "switch_icon"
                ) { isChecked ->
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = iconColor
                        )
                    }
                }
            }
        }
    }
}
