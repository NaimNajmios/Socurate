package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Animated Paste Action Icon.
 * Features a delightful "wiggle" animation to invite interaction when the input is empty.
 * Uses the custom Neo paste icon for better semantic clarity.
 */
@Composable
fun PasteAction(
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val rotation = remember { Animatable(0f) }

    // Interaction state for proper press handling
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press animation: Scale down when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "paste_press_scale"
    )

    // Wiggle animation loop: Gentle reminder to paste content
    LaunchedEffect(isPressed) {
        if (isPressed) {
            // Stop wiggling immediately if pressed
            rotation.snapTo(0f)
        } else {
            // Wait before starting the loop again
            delay(4000)
            while (isActive) {
                // Wiggle sequence
                rotation.animateTo(15f, spring(stiffness = Spring.StiffnessHigh))
                rotation.animateTo(-15f, spring(stiffness = Spring.StiffnessHigh))
                rotation.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                delay(4000) // Pause between wiggles
            }
        }
    }

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPaste()
        },
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_paste),
            contentDescription = "Paste from clipboard",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = rotation.value
                }
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
                    (fadeIn() + expandHorizontally()) with (fadeOut() + shrinkHorizontally())
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
                    scaleIn() with scaleOut()
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
