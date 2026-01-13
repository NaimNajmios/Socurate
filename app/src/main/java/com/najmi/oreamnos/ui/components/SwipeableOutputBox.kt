package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

/**
 * Swipeable output box with gesture support and delightful micro-interactions:
 * - Swipe left to reveal Copy action
 * - Swipe right to reveal Share action
 * - Features haptic feedback, scaling animations, and threshold snapping
 * - Includes "Shimmy" entrance animation to teach gestures
 * - Implements "Success Snap": Card locks open and icon morphs to checkmark on success
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

    // Success Snap State
    var isSuccessLocked by remember { mutableStateOf(false) }
    var successDirection by remember { mutableStateOf(0) } // -1: Copy, 1: Share
    val successColor = Color(0xFF4CAF50) // Success Green

    // State to track if we've crossed the threshold to trigger haptics only once
    var isPastThreshold by remember { mutableStateOf(false) }

    // Track user interaction to cancel the shimmy animation
    var isInteracted by remember { mutableStateOf(false) }

    // "Shimmy" Entrance Animation
    LaunchedEffect(Unit) {
        delay(600)
        if (!isInteracted) offsetX = 50f
        delay(500)
        if (!isInteracted) offsetX = 0f
        delay(200)
        if (!isInteracted) offsetX = -50f
        delay(500)
        if (!isInteracted) offsetX = 0f
    }

    // Auto-reset success lock after delay
    LaunchedEffect(isSuccessLocked) {
        if (isSuccessLocked) {
            delay(1000) // Hold success state for 1 second
            if (isSuccessLocked) { // Check if still locked (not interrupted)
                isSuccessLocked = false
                offsetX = 0f
                successDirection = 0
            }
        }
    }

    // Animate offset: If locked, target the threshold; otherwise track drag/reset
    val targetOffset = if (isSuccessLocked) successDirection * swipeThreshold else offsetX
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = if (isSuccessLocked) Spring.DampingRatioMediumBouncy else Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
    ) {
        // --- BACKGROUND LAYER (Actions) ---

        // Helper to render action background
        @Composable
        fun ActionBackground(
            direction: Int, // 1 for Share (Left aligned), -1 for Copy (Right aligned)
            isActive: Boolean,
            isSuccess: Boolean,
            iconRes: Int,
            description: String
        ) {
            val align = if (direction == 1) Alignment.CenterStart else Alignment.CenterEnd
            // Reveal alpha based on drag progress
            val revealAlpha = if (direction == 1) {
                (offsetX / swipeThreshold).coerceIn(0f, 1f)
            } else {
                (-offsetX / swipeThreshold).coerceIn(0f, 1f)
            }
            // Force full alpha if success locked
            val finalAlpha = if (isSuccess) 1f else revealAlpha

            Box(
                modifier = Modifier
                    .align(align)
                    .padding(start = if (direction == 1) 24.dp else 0.dp, end = if (direction == -1) 24.dp else 0.dp)
                    .alpha(if (offsetX * direction > 0 || isSuccess) finalAlpha else 0f),
                contentAlignment = Alignment.Center
            ) {
                val scaleState by animateFloatAsState(if (isActive || isSuccess) 1.2f else 1.0f, label = "scale")
                // Rotate icon based on drag distance for "rolling" effect
                val rotateState by animateFloatAsState(
                    if (isSuccess) 0f else (offsetX / swipeThreshold * 15f),
                    label = "rotate"
                )

                val iconColor by animateColorAsState(
                    if (isActive || isSuccess) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "icon_color"
                )
                val bgColor by animateColorAsState(
                    if (isSuccess) successColor
                    else if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    label = "bg_color"
                )
                val bgScale by animateFloatAsState(if (isActive || isSuccess) 1f else 0f, label = "bg_scale")

                // Background Circle
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

                // Icon Morph
                AnimatedContent(
                    targetState = isSuccess,
                    transitionSpec = {
                        (scaleIn() + fadeIn()) with (scaleOut() + fadeOut())
                    },
                    label = "icon_morph"
                ) { success ->
                    if (success) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = description,
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
                }
            }
        }

        // Share Action (Left side, revealed when swiping Right)
        ActionBackground(
            direction = 1,
            isActive = offsetX > swipeThreshold,
            isSuccess = isSuccessLocked && successDirection == 1,
            iconRes = R.drawable.ic_share,
            description = "Share"
        )

        // Copy Action (Right side, revealed when swiping Left)
        ActionBackground(
            direction = -1,
            isActive = offsetX < -swipeThreshold,
            isSuccess = isSuccessLocked && successDirection == -1,
            iconRes = R.drawable.ic_copy,
            description = "Copy"
        )

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
                            isInteracted = true
                            // If user touches while success locked, unlock immediately
                            if (isSuccessLocked) {
                                isSuccessLocked = false
                                offsetX = 0f
                                successDirection = 0
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            isInteracted = true
                            if (!isSuccessLocked) {
                                // Add resistance
                                val resistance = 1f - (abs(offsetX) / (swipeThreshold * 2)).coerceIn(0f, 0.5f)
                                offsetX += dragAmount * resistance

                                // Haptics
                                val currentlyPastThreshold = abs(offsetX) > swipeThreshold
                                if (currentlyPastThreshold != isPastThreshold) {
                                    if (currentlyPastThreshold) {
                                        hapticHelper.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                    }
                                    isPastThreshold = currentlyPastThreshold
                                }
                            }
                        },
                        onDragEnd = {
                            if (abs(offsetX) > swipeThreshold) {
                                // Trigger Success
                                hapticHelper.onCopy()
                                isSuccessLocked = true
                                if (offsetX > 0) {
                                    successDirection = 1
                                    onShare()
                                } else {
                                    successDirection = -1
                                    onCopy()
                                }
                                // Offset stays at current value (drag point) initially,
                                // but targetOffset will snap it to threshold via state
                            } else {
                                // Reset
                                offsetX = 0f
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
            val primaryColor = MaterialTheme.colorScheme.primary
            val parsedText = remember(outputText, primaryColor) {
                com.najmi.oreamnos.utils.MarkdownUtils.parseMarkdownToAnnotatedString(outputText, primaryColor)
            }

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

            // Fading Chevron Hints
            val shimmyVisible = !isInteracted && abs(animatedOffset) > 10f
            val hintAlpha by animateFloatAsState(if (shimmyVisible) 0.6f else 0f, label = "hint_alpha")

            if (hintAlpha > 0f) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .alpha(hintAlpha)
                )

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
