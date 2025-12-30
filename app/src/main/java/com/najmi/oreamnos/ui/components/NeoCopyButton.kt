package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A specialized NeoOutlinedButton for Copy actions that provides visual feedback.
 * Morphs from "COPY" to "COPIED!" with a check icon and color change.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NeoCopyButton(
    text: String = "COPY",
    contentDescription: String = "Copy to clipboard",
    modifier: Modifier = Modifier,
    onCopy: () -> Unit
) {
    var isCopied by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Reset copied state after 2 seconds
    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val targetColor = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "CopyButtonColor"
    )

    OutlinedButton(
        onClick = {
            if (!isCopied) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onCopy()
                isCopied = true
            }
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
            targetState = isCopied,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            },
            label = "CopyContent"
        ) { copied ->
            if (copied) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Copied",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF4CAF50) // Green color
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
