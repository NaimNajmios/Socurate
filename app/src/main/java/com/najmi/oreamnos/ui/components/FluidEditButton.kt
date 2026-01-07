package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A specialized NeoOutlinedButton for Edit/Save actions that provides visual feedback.
 * Morphs between "EDIT" and "SAVE" states with smooth text transitions.
 * Matches the text-only style of NeoCopyButton and NeoButton.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FluidEditButton(
    isEditing: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Animate color: Primary when editing (active state), OnSurface when reading
    val targetColor = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "EditButtonColor"
    )

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onToggle()
        },
        modifier = modifier,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, animatedColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = animatedColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        AnimatedContent(
            targetState = isEditing,
            transitionSpec = {
                if (targetState) {
                    // Edit -> Save: Slide up
                    (slideInVertically { height -> height } + fadeIn()) with
                    (slideOutVertically { height -> -height } + fadeOut())
                } else {
                    // Save -> Edit: Slide down
                    (slideInVertically { height -> -height } + fadeIn()) with
                    (slideOutVertically { height -> height } + fadeOut())
                }
            },
            label = "EditContent"
        ) { editing ->
            Text(
                text = if (editing) "SAVE" else "EDIT",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
