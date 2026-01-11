package com.najmi.oreamnos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.R
import com.najmi.oreamnos.utils.HapticHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Swipeable output box with gesture support and delightful micro-interactions:
 * - Swipe left to reveal Copy action
 * - Swipe right to reveal Share action
 * - Features haptic feedback, scaling animations, and threshold snapping
 * - Includes "Shimmy" entrance animation to teach gestures
 *
 * OPTIMIZED: Uses graphicsLayer and Animatable to decouple drag animations from recomposition.
 * The entire swipe interaction happens in the layout/draw phase, ensuring 60fps/120fps smoothness
 * even on lower-end devices.
 */
@Composable
fun SwipeableOutputBox(
    outputText: String,
    textSize: Int,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticHelper = remember { HapticHelper(context) }

    // OPTIMIZATION: Use Animatable as the single source of truth for visual position.
    // This allows us to update the UI via graphicsLayer without triggering recomposition.
    val translationAnim = remember { Animatable(0f) }

    // Track logic state separately for threshold checks during drag
    val swipeThreshold = 150f

    // State to track if we've crossed the threshold to trigger haptics only once
    var isPastThreshold by remember { mutableStateOf(false) }

    // Track user interaction to cancel the shimmy animation
    var isInteracted by remember { mutableStateOf(false) }

    // "Shimmy" Entrance Animation: Teaches the user that the card is swipeable
    // OPTIMIZATION: Uses animateTo sequences instead of State changes + delays to avoid recomposition
    LaunchedEffect(Unit) {
        val springConfig = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )

        delay(600) // Wait for card entrance

        if (!isInteracted) translationAnim.animateTo(50f, springConfig) // Slide Right
        delay(200)
        if (!isInteracted) translationAnim.animateTo(0f, springConfig)

        delay(100)
        if (!isInteracted) translationAnim.animateTo(-50f, springConfig) // Slide Left
        delay(200)
        if (!isInteracted) translationAnim.animateTo(0f, springConfig)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp) // Ensure minimum height for swipe area
    ) {
        // --- BACKGROUND LAYER (Actions) ---

        // OPTIMIZATION: Derived states ensure we only recompose the action icons when the threshold is crossed,
        // not on every pixel of the drag.
        val isRightActive by remember { derivedStateOf { translationAnim.value > swipeThreshold } }
        val isLeftActive by remember { derivedStateOf { translationAnim.value < -swipeThreshold } }

        // Share Action (Left side, revealed when swiping Right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .graphicsLayer {
                    // Reveal opacity based on drag distance
                    val currentOffset = translationAnim.value
                    alpha = if (currentOffset > 0) (currentOffset / swipeThreshold).coerceIn(0f, 1f) else 0f
                },
            contentAlignment = Alignment.Center
        ) {
            val scaleState = animateFloatAsState(if (isRightActive) 1.2f else 1.0f, label = "share_scale")
            val rotateState = animateFloatAsState(if (isRightActive) 15f else 0f, label = "share_rotate")
            val iconColor = animateColorAsState(
                if (isRightActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "share_icon_color"
            )
            val bgScale = animateFloatAsState(if (isRightActive) 1f else 0f, label = "share_bg_scale")

            // Background Circle for visual pop
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = bgScale.value
                        scaleY = bgScale.value
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "Share",
                tint = iconColor.value,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        scaleX = scaleState.value
                        scaleY = scaleState.value
                        rotationZ = rotateState.value
                    }
            )
        }

        // Copy Action (Right side, revealed when swiping Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .graphicsLayer {
                    val currentOffset = translationAnim.value
                    alpha = if (currentOffset < 0) (-currentOffset / swipeThreshold).coerceIn(0f, 1f) else 0f
                },
            contentAlignment = Alignment.Center
        ) {
            val scaleState = animateFloatAsState(if (isLeftActive) 1.2f else 1.0f, label = "copy_scale")
            val rotateState = animateFloatAsState(if (isLeftActive) -15f else 0f, label = "copy_rotate")
            val iconColor = animateColorAsState(
                if (isLeftActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "copy_icon_color"
            )
             val bgScale = animateFloatAsState(if (isLeftActive) 1f else 0f, label = "copy_bg_scale")

             // Background Circle for visual pop
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = bgScale.value
                        scaleY = bgScale.value
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Icon(
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = "Copy",
                tint = iconColor.value,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        scaleX = scaleState.value
                        scaleY = scaleState.value
                        rotationZ = rotateState.value
                    }
            )
        }

        // --- FOREGROUND LAYER (Content) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // OPTIMIZATION: Read animation value in draw phase only
                .graphicsLayer {
                    translationX = translationAnim.value
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isInteracted = true // Cancel shimmy on first touch
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            isInteracted = true

                            val current = translationAnim.value
                            // Add resistance as we drag further
                            val resistance = 1f - (abs(current) / (swipeThreshold * 2)).coerceIn(0f, 0.5f)
                            val target = current + dragAmount * resistance

                            // Snap immediately for responsiveness (inside coroutine to access suspend snapTo)
                            scope.launch { translationAnim.snapTo(target) }

                            // Haptic feedback logic
                            val currentlyPastThreshold = abs(target) > swipeThreshold
                            if (currentlyPastThreshold != isPastThreshold) {
                                if (currentlyPastThreshold) {
                                    hapticHelper.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                }
                                isPastThreshold = currentlyPastThreshold
                            }
                        },
                        onDragEnd = {
                            val current = translationAnim.value
                            if (abs(current) > swipeThreshold) {
                                // Trigger action
                                hapticHelper.onCopy() // Success haptic
                                if (current > 0) onShare() else onCopy()
                            }
                            // Reset with spring
                            scope.launch {
                                translationAnim.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                            isPastThreshold = false
                        }
                    )
                }
                .heightIn(max = 400.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Memoize parsed text to avoid re-parsing on every frame during swipe
            val primaryColor = MaterialTheme.colorScheme.primary
            val parsedText = remember(outputText, primaryColor) {
                com.najmi.oreamnos.utils.MarkdownUtils.parseMarkdownToAnnotatedString(outputText, primaryColor)
            }

            // Using fully qualified name to avoid import ambiguity
            androidx.compose.foundation.text.selection.SelectionContainer {
                com.najmi.oreamnos.ui.components.TypewriterText(
                    text = parsedText,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = textSize.sp,
                        lineHeight = (textSize * 1.5f).sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )
            }

            // Fading Chevron Hints - Visible only during shimmy to teach direction
            // OPTIMIZATION: Derived state to prevent recomposition until conditions actually change
            val shimmyVisible by remember {
                derivedStateOf { !isInteracted && abs(translationAnim.value) > 10f }
            }
            val hintAlphaState = animateFloatAsState(if (shimmyVisible) 0.6f else 0f, label = "hint_alpha")

            // Use Box to hold icons and apply alpha via graphicsLayer
            if (hintAlphaState.value > 0f) {
                Box(
                    modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = hintAlphaState.value }
                ) {
                    // Left Hint (Pointing Right to indicate Swipe Right)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )

                    // Right Hint (Pointing Left to indicate Swipe Left)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                    )
                }
            }
        }
    }
}
