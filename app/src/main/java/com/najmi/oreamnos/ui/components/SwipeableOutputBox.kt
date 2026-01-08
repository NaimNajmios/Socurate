package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.R
import com.najmi.oreamnos.utils.HapticHelper
import kotlinx.coroutines.delay
import kotlin.math.abs

private enum class SwipeAction { COPY, SHARE }

/**
 * Swipeable output box with gesture support and delightful micro-interactions:
 * - Swipe left to reveal Copy action (with Success Snap confirmation)
 * - Swipe right to reveal Share action
 * - Features haptic feedback, scaling animations, and threshold snapping
 * - Includes "Shimmy" entrance animation to teach gestures
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeableOutputBox(
    outputText: String,
    textSize: Int,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val hapticHelper = remember { HapticHelper(context) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f

    // State to track if we've crossed the threshold to trigger haptics only once
    var isPastThreshold by remember { mutableStateOf(false) }

    // Track user interaction to cancel the shimmy animation
    var isInteracted by remember { mutableStateOf(false) }

    // Track confirmed action for visual feedback
    var confirmedAction by remember { mutableStateOf<SwipeAction?>(null) }

    // "Shimmy" Entrance Animation: Teaches the user that the card is swipeable
    LaunchedEffect(Unit) {
        delay(600) // Wait for card entrance
        if (!isInteracted) offsetX = 50f // Slide Right (Reveal Share)
        delay(500)
        if (!isInteracted) offsetX = 0f
        delay(200)
        if (!isInteracted) offsetX = -50f // Slide Left (Reveal Copy)
        delay(500)
        if (!isInteracted) offsetX = 0f
    }

    // Handle Copy Confirmation Reset
    LaunchedEffect(confirmedAction) {
        if (confirmedAction == SwipeAction.COPY) {
            delay(1000) // Hold success state for 1 second
            offsetX = 0f
            delay(300) // Wait for spring animation
            confirmedAction = null
            isPastThreshold = false
        }
    }

    // Animate offset back to 0 when released
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp) // Ensure minimum height for swipe area
    ) {
        // --- BACKGROUND LAYER (Actions) ---
        // Share Action (Left side, revealed when swiping Right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .alpha(if (offsetX > 0) (offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f),
            contentAlignment = Alignment.Center
        ) {
            val isActive = offsetX > swipeThreshold
            val scaleState by animateFloatAsState(if (isActive) 1.2f else 1.0f, label = "share_scale")
            val rotateState by animateFloatAsState(if (isActive) 15f else 0f, label = "share_rotate")
            val bgScale by animateFloatAsState(if (isActive) 1f else 0f, label = "share_bg_scale")
            val iconColor by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "share_icon_color"
            )

            // Background Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = bgScale
                        scaleY = bgScale
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "Share",
                tint = iconColor,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        scaleX = scaleState
                        scaleY = scaleState
                        rotationZ = rotateState
                    }
            )
        }

        // Copy Action (Right side, revealed when swiping Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .alpha(if (offsetX < 0) (-offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f),
            contentAlignment = Alignment.Center
        ) {
            val isActive = offsetX < -swipeThreshold || confirmedAction == SwipeAction.COPY
            val isConfirmed = confirmedAction == SwipeAction.COPY

            val scaleState by animateFloatAsState(if (isActive) 1.2f else 1.0f, label = "copy_scale")
            val rotateState by animateFloatAsState(if (isActive && !isConfirmed) -15f else 0f, label = "copy_rotate")

            // Visual Confirmation: Green Background
            val bgColor by animateColorAsState(
                if (isConfirmed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                label = "copy_bg_color"
            )
            val bgScale by animateFloatAsState(if (isActive) 1f else 0f, label = "copy_bg_scale")

            // Icon Color: Dark when dragging, White when active/confirmed
            val iconColor by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "copy_icon_color"
            )

             // Background Circle for visual pop
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = bgScale
                        scaleY = bgScale
                    }
                    .clip(CircleShape)
                    .background(bgColor)
            )

            // Morph between Copy Icon and Checkmark
            AnimatedContent(
                targetState = isConfirmed,
                transitionSpec = {
                    scaleIn() with scaleOut()
                },
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scaleState
                        scaleY = scaleState
                        rotationZ = rotateState
                    },
                label = "copy_icon_morph"
            ) { confirmed ->
                if (confirmed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Copied!",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "Copy",
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // --- FOREGROUND LAYER (Content) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = animatedOffset
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isInteracted = true // Cancel shimmy on first touch
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            // Disable dragging if currently confirming an action
                            if (confirmedAction != null) return@detectHorizontalDragGestures

                            change.consume()
                            isInteracted = true
                            // Add resistance as we drag further
                            val resistance = 1f - (abs(offsetX) / (swipeThreshold * 2)).coerceIn(0f, 0.5f)
                            offsetX += dragAmount * resistance

                            // Haptic feedback logic
                            val currentlyPastThreshold = abs(offsetX) > swipeThreshold
                            if (currentlyPastThreshold != isPastThreshold) {
                                if (currentlyPastThreshold) {
                                    hapticHelper.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                }
                                isPastThreshold = currentlyPastThreshold
                            }
                        },
                        onDragEnd = {
                            // Prevent interfering if animation is running
                            if (confirmedAction != null) return@detectHorizontalDragGestures

                            if (abs(offsetX) > swipeThreshold) {
                                if (offsetX > 0) {
                                    // SHARE: Standard trigger (System Sheet overlays)
                                    hapticHelper.onCopy()
                                    onShare()
                                    offsetX = 0f
                                    isPastThreshold = false
                                } else {
                                    // COPY: "Success Snap" Interaction
                                    // Lock the offset and trigger visual confirmation
                                    hapticHelper.onCopy()
                                    confirmedAction = SwipeAction.COPY
                                    // Snap to a nice visible position for the confirmation
                                    offsetX = -swipeThreshold * 1.1f
                                    onCopy()
                                }
                            } else {
                                // Cancelled - Reset
                                offsetX = 0f
                                isPastThreshold = false
                            }
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
            val shimmyVisible = !isInteracted && abs(animatedOffset) > 10f
            val hintAlpha by animateFloatAsState(if (shimmyVisible) 0.6f else 0f, label = "hint_alpha")

            if (hintAlpha > 0f) {
                // Left Hint (Pointing Right to indicate Swipe Right)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .alpha(hintAlpha)
                )

                // Right Hint (Pointing Left to indicate Swipe Left)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .alpha(hintAlpha)
                )
            }
        }
    }
}
