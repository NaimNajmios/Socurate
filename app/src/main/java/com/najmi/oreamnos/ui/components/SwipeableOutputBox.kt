package com.najmi.oreamnos.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
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
 */
@Composable
fun SwipeableOutputBox(
    outputText: String,
    textSize: Int,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val hapticHelper = remember { HapticHelper(context) }

    // OPTIMIZATION: Use mutableFloatStateOf but access via State object in graphicsLayer
    // to avoid recomposition during drag.
    val offsetXState = remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f

    // State to track if we've crossed the threshold to trigger haptics only once
    var isPastThreshold by remember { mutableStateOf(false) }

    // Track user interaction to cancel the shimmy animation
    var isInteracted by remember { mutableStateOf(false) }

    // "Shimmy" Entrance Animation: Teaches the user that the card is swipeable
    LaunchedEffect(Unit) {
        delay(600) // Wait for card entrance
        if (!isInteracted) offsetXState.floatValue = 50f // Slide Right (Reveal Share)
        delay(500)
        if (!isInteracted) offsetXState.floatValue = 0f
        delay(200)
        if (!isInteracted) offsetXState.floatValue = -50f // Slide Left (Reveal Copy)
        delay(500)
        if (!isInteracted) offsetXState.floatValue = 0f
    }

    // OPTIMIZATION: Capture animation state to read inside graphicsLayer.
    // We avoid 'by' delegation here to prevent recomposition of the parent
    // when the value changes every frame.
    val animatedOffsetState = animateFloatAsState(
        targetValue = offsetXState.floatValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )

    // OPTIMIZATION: Derived state to prevent recomposition when offset changes slightly
    // but threshold isn't crossed (shimmy visibility logic).
    val shimmyVisible by remember {
        derivedStateOf {
            !isInteracted && abs(animatedOffsetState.value) > 10f
        }
    }

    val hintAlphaState = animateFloatAsState(
        targetValue = if (shimmyVisible) 0.6f else 0f,
        label = "hint_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp) // Ensure minimum height for swipe area
    ) {
        // --- BACKGROUND LAYER (Actions) ---

        // Share Action (Left side, revealed when swiping Right)
        ActionBackground(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp),
            isVisible = { offset -> offset > 0 },
            alphaCalculation = { offset -> (offset / swipeThreshold).coerceIn(0f, 1f) },
            isActiveCalculation = { offset -> offset > swipeThreshold },
            icon = painterResource(R.drawable.ic_share),
            contentDescription = "Share",
            rotationTarget = 15f,
            offsetXState = offsetXState
        )

        // Copy Action (Right side, revealed when swiping Left)
        ActionBackground(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp),
            isVisible = { offset -> offset < 0 },
            alphaCalculation = { offset -> (-offset / swipeThreshold).coerceIn(0f, 1f) },
            isActiveCalculation = { offset -> offset < -swipeThreshold },
            icon = painterResource(R.drawable.ic_copy),
            contentDescription = "Copy",
            rotationTarget = -15f,
            offsetXState = offsetXState
        )

        // --- FOREGROUND LAYER (Content) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    // OPTIMIZATION: Read state inside graphicsLayer to skip recomposition
                    translationX = animatedOffsetState.value
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isInteracted = true // Cancel shimmy on first touch
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            isInteracted = true

                            val currentOffset = offsetXState.floatValue
                            // Add resistance as we drag further
                            val resistance = 1f - (abs(currentOffset) / (swipeThreshold * 2)).coerceIn(0f, 0.5f)
                            offsetXState.floatValue = currentOffset + (dragAmount * resistance)

                            // Haptic feedback logic
                            val currentlyPastThreshold = abs(offsetXState.floatValue) > swipeThreshold
                            if (currentlyPastThreshold != isPastThreshold) {
                                if (currentlyPastThreshold) {
                                    hapticHelper.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                }
                                isPastThreshold = currentlyPastThreshold
                            }
                        },
                        onDragEnd = {
                            val currentOffset = offsetXState.floatValue
                            if (abs(currentOffset) > swipeThreshold) {
                                // Trigger action
                                hapticHelper.onCopy() // Success haptic
                                if (currentOffset > 0) onShare() else onCopy()
                            }
                            // Reset
                            offsetXState.floatValue = 0f
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
            // OPTIMIZATION: Use graphicsLayer for alpha to avoid recomposition
            if (hintAlphaState.value > 0f || shimmyVisible) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min=24.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .graphicsLayer { alpha = hintAlphaState.value }
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .graphicsLayer { alpha = hintAlphaState.value }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionBackground(
    modifier: Modifier,
    isVisible: (Float) -> Boolean,
    alphaCalculation: (Float) -> Float,
    isActiveCalculation: (Float) -> Boolean,
    icon: Painter,
    contentDescription: String,
    rotationTarget: Float,
    offsetXState: State<Float>
) {
    // OPTIMIZATION: Use derivedStateOf to isolate active state changes.
    // This ensures we only recompose when 'isActive' actually flips,
    // not on every drag pixel.
    val isActive by remember {
        derivedStateOf { isActiveCalculation(offsetXState.value) }
    }

    val scaleState = animateFloatAsState(if (isActive) 1.2f else 1.0f, label = "scale")
    val rotateState = animateFloatAsState(if (isActive) rotationTarget else 0f, label = "rotate")
    val bgScaleState = animateFloatAsState(if (isActive) 1f else 0f, label = "bg_scale")
    val iconColor by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "icon_color"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .graphicsLayer {
                val offset = offsetXState.value
                alpha = if (isVisible(offset)) alphaCalculation(offset) else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        // Background Circle for visual pop
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    // Read .value inside graphicsLayer
                    scaleX = bgScaleState.value
                    scaleY = bgScaleState.value
                }
                .clip(CircleShape)
                .background(primaryColor)
        )

        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    // Read .value inside graphicsLayer
                    scaleX = scaleState.value
                    scaleY = scaleState.value
                    rotationZ = rotateState.value
                }
        )
    }
}
