package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animated Paste Action Icon.
 * Pulses gently to invite interaction when the input is empty.
 */
@Composable
fun PasteAction(
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "paste_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paste_scale"
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onPaste()
        },
        modifier = modifier.scale(scale)
    ) {
        Icon(
            imageVector = Icons.Default.Add, // Using Add as a "Add Content" metaphor
            contentDescription = "Paste from clipboard",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Fluid Clear Action Icon.
 * Morphs from Close (X) to Delete (Trash) with a "CLEAR?" label for confirmation.
 * Prevents accidental data loss by requiring a double-tap.
 *
 * Features:
 * - Minimum 48dp touch target for accessibility
 * - Distinct visual states (Normal -> Error Container)
 * - Clear semantic role for screen readers
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClearAction(
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isConfirming by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Reset confirmation state after 3 seconds
    LaunchedEffect(isConfirming) {
        if (isConfirming) {
            delay(3000)
            isConfirming = false
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isConfirming) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
        label = "clear_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isConfirming) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "clear_content"
    )

    // Using Box to enforce minimum touch target size (48dp) while centering the visual content
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = if (isConfirming) "Confirm Clear" else "Prepare to Clear"
            ) {
                if (isConfirming) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClear()
                    isConfirming = false
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isConfirming = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual container with padding and shape
        Row(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isConfirming,
                transitionSpec = {
                    (fadeIn() + expandHorizontally()) togetherWith (fadeOut() + shrinkHorizontally())
                },
                label = "clear_text_morph"
            ) { confirming ->
                if (confirming) {
                    Text(
                        "CLEAR?",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            AnimatedContent(
                targetState = isConfirming,
                transitionSpec = {
                    scaleIn() togetherWith scaleOut()
                },
                label = "clear_icon_morph"
            ) { confirming ->
                if (confirming) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Confirm clear",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear text",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
