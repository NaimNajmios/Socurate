package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A polished Clear button with a confirmation step to prevent accidental data loss.
 * Morphs from "CLEAR" to "CONFIRM?" on first click.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InputClearButton(
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isConfirming by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Reset confirmation state after 3 seconds of inactivity
    LaunchedEffect(isConfirming) {
        if (isConfirming) {
            delay(3000)
            isConfirming = false
        }
    }

    TextButton(
        onClick = {
            if (isConfirming) {
                onClear()
                isConfirming = false
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isConfirming = true
            }
        },
        modifier = modifier.animateContentSize(),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        AnimatedContent(
            targetState = isConfirming,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200))
            },
            label = "clear_confirm"
        ) { confirming ->
            if (confirming) {
                Text(
                    "CONFIRM?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                Text(
                    "CLEAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
