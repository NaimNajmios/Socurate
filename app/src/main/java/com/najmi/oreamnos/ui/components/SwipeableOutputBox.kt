package com.najmi.oreamnos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.R
import com.najmi.oreamnos.utils.HapticHelper
import kotlin.math.abs

/**
 * Swipeable output box with gesture support and delightful micro-interactions:
 * - Swipe left to reveal Copy action
 * - Swipe right to reveal Share action
 * - Features haptic feedback, scaling animations, and threshold snapping
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f

    // State to track if we've crossed the threshold to trigger haptics only once
    var isPastThreshold by remember { mutableStateOf(false) }

    // Animate offset back to 0 when released
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
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
                .alpha(if (offsetX > 0) (offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f)
        ) {
            val isActive = offsetX > swipeThreshold
            val scale by animateFloatAsState(if (isActive) 1.2f else 1.0f, label = "share_scale")
            val color by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "share_color"
            )

            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "Share",
                tint = color,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
        }

        // Copy Action (Right side, revealed when swiping Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .alpha(if (offsetX < 0) (-offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f)
        ) {
            val isActive = offsetX < -swipeThreshold
            val scale by animateFloatAsState(if (isActive) 1.2f else 1.0f, label = "copy_scale")
            val color by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "copy_color"
            )

            Icon(
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = "Copy",
                tint = color,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
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
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
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
                            if (abs(offsetX) > swipeThreshold) {
                                // Trigger action
                                hapticHelper.onCopy() // Success haptic
                                if (offsetX > 0) onShare() else onCopy()
                            }
                            // Reset
                            offsetX = 0f
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
                parseMarkdownToAnnotatedString(outputText, primaryColor)
            }

            // Using fully qualified name to avoid import ambiguity if TypewriterText isn't imported
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
        }
    }
}

/**
 * Parses markdown formatting and converts to AnnotatedString for rich text display.
 * Supports: **bold**, *italic*, _italic_, ## Headers, - lists, * lists.
 * Kept private to this file or component to allow usage inside remember block without @Composable constraint.
 */
private fun parseMarkdownToAnnotatedString(text: String, primaryColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")

        lines.forEachIndexed { lineIndex, line ->
            // Check for header (## Header)
            if (line.trimStart().startsWith("## ")) {
                val headerText = line.trimStart().removePrefix("## ")
                withStyle(
                    style = SpanStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                ) {
                    append(headerText)
                }
            }
            // Check for bullet list (- item, * item, or • item)
            else if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
                val bulletText = line.trimStart().drop(2)
                append("• ") // Convert to bullet
                parseInlineFormatting(bulletText)
            }
            // Check for already-bulleted line (• U+2022)
            else if (line.trimStart().startsWith("\u2022 ") || line.trimStart().startsWith("\u2022")) {
                // Keep the bullet character and parse the rest
                val trimmed = line.trimStart()
                val bulletText = if (trimmed.length > 1 && trimmed[1] == ' ') trimmed.drop(2) else trimmed.drop(1)
                append("• ")
                parseInlineFormatting(bulletText)
            }
            else {
                // Parse inline formatting (bold, italic)
                parseInlineFormatting(line)
            }

            // Add newline except for last line
            if (lineIndex < lines.size - 1) {
                append("\n")
            }
        }
    }
}

/**
 * Helper function to parse inline formatting (bold and italic)
 */
private fun AnnotatedString.Builder.parseInlineFormatting(text: String) {
    var currentIndex = 0

    while (currentIndex < text.length) {
        // Look for bold (**text**)
        val boldStart = text.indexOf("**", currentIndex)
        // Look for italic (*text* or _text_)
        val italicStarStart = text.indexOf("*", currentIndex).let {
            if (it != -1 && it + 1 < text.length && text[it + 1] == '*') -1 else it
        }
        val italicUnderStart = text.indexOf("_", currentIndex).let {
            if (it != -1 && it + 1 < text.length && text[it + 1] == '_') -1 else it
        }

        // Find earliest formatting marker
        val nextFormat = listOf(
            boldStart to "bold",
            italicStarStart to "italic_star",
            italicUnderStart to "italic_under"
        ).filter { it.first != -1 }.minByOrNull { it.first }

        if (nextFormat == null) {
            // No more formatting, append rest
            append(text.substring(currentIndex))
            break
        }

        val (formatStart, formatType) = nextFormat

        // Append text before formatting
        append(text.substring(currentIndex, formatStart))

        when (formatType) {
            "bold" -> {
                val boldEnd = text.indexOf("**", formatStart + 2)
                if (boldEnd != -1) {
                    val boldText = text.substring(formatStart + 2, boldEnd)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldText)
                    }
                    currentIndex = boldEnd + 2
                } else {
                    append("**")
                    currentIndex = formatStart + 2
                }
            }
            "italic_star" -> {
                val italicEnd = text.indexOf("*", formatStart + 1)
                if (italicEnd != -1) {
                    val italicText = text.substring(formatStart + 1, italicEnd)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                    currentIndex = italicEnd + 1
                } else {
                    append("*")
                    currentIndex = formatStart + 1
                }
            }
            "italic_under" -> {
                val italicEnd = text.indexOf("_", formatStart + 1)
                if (italicEnd != -1) {
                    val italicText = text.substring(formatStart + 1, italicEnd)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                    currentIndex = italicEnd + 1
                } else {
                    append("_")
                    currentIndex = formatStart + 1
                }
            }
        }
    }
}
