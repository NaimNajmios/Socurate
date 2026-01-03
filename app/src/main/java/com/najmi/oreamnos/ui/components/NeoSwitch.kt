package com.najmi.oreamnos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
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
    val alignmentBias by animateFloatAsState(
        targetValue = if (checked) 1f else -1f,
        animationSpec = tween(durationMillis = 200),
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

    val thumbColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "thumb_color"
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
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
                    .background(thumbColor, RoundedCornerShape(0.dp))
            )
        }
    }
}
