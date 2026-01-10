package com.najmi.oreamnos.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Delightful Empty State Card
 * Features a "breathing" and "floating" icon animation to invite interaction.
 * Includes a "Smart Paste" button that provides immediate fluid feedback.
 */
@Composable
fun EmptyStateCard(onPaste: (String) -> Unit) {
    // Micro-interactions: Breathing and Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "emptyStateAnimations")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )

    NeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_empty_state),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .graphicsLayer {
                        translationY = offsetY
                    },
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "READY TO GENERATE",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Paste content or enter a URL above",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            SmartPasteButton(onPaste = onPaste)
        }
    }
}

/**
 * A fluid, smart button that handles paste actions with delightful feedback.
 * - Success: Morphs to checkmark, green color, haptic tick.
 * - Error (Empty): Shakes, red color, haptic buzz.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SmartPasteButton(
    onPaste: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // States: Idle, Success, Error
    var pasteState by remember { mutableStateOf<PasteState>(PasteState.Idle) }

    // Shake Animation for Error
    val shakeOffset = remember { Animatable(0f) }

    // Logic to handle paste
    val handlePaste = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text?.toString()

            if (!text.isNullOrBlank()) {
                // Success Flow
                pasteState = PasteState.Success
                // Distinct, crisp feedback for success
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                scope.launch {
                    // Slight delay to allow the user to perceive the success state
                    // before the card disappears (as the parent state changes).
                    // Reduced to 400ms for better responsiveness.
                    delay(400)
                    onPaste(text)

                    // Reset state (if component survives)
                    delay(500)
                    pasteState = PasteState.Idle
                }
            } else {
                // Empty Text Flow
                pasteState = PasteState.Error
                // Error feedback
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                scope.launch {
                    // Trigger shake
                    shakeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 500
                            0f at 0
                            -10f at 50
                            10f at 100
                            -10f at 150
                            10f at 200
                            -5f at 250
                            5f at 300
                            0f at 500
                        }
                    )
                    delay(1000)
                    pasteState = PasteState.Idle
                }
            }
        } else {
            // Empty Clipboard Flow
            pasteState = PasteState.Error
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scope.launch {
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 500
                        0f at 0
                        -10f at 50
                        10f at 100
                        -10f at 150
                        10f at 200
                        -5f at 250
                        5f at 300
                        0f at 500
                    }
                )
                delay(1000)
                pasteState = PasteState.Idle
            }
        }
    }

    // Colors based on state
    val targetColor = when (pasteState) {
        PasteState.Idle -> MaterialTheme.colorScheme.onSurface
        PasteState.Success -> Color(0xFF4CAF50) // Success Green
        PasteState.Error -> MaterialTheme.colorScheme.error
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "PasteButtonColor"
    )

    OutlinedButton(
        onClick = { if (pasteState == PasteState.Idle) handlePaste() },
        modifier = modifier
            .graphicsLayer {
                translationX = shakeOffset.value
            },
        shape = RoundedCornerShape(0.dp), // Match NeoOutlinedButton style
        border = BorderStroke(2.dp, animatedColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = animatedColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        AnimatedContent(
            targetState = pasteState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f)) with
                (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f))
            },
            label = "PasteButtonContent"
        ) { state ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    PasteState.Idle -> {
                        Text(
                            text = "PASTE FROM CLIPBOARD",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    PasteState.Success -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "PASTED!",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    PasteState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "CLIPBOARD EMPTY",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

private enum class PasteState {
    Idle, Success, Error
}
