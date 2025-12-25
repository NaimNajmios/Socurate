package com.najmi.oreamnos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated Success Checkmark
 * Displays a circular progress animation followed by a checkmark draw animation.
 * Used to provide visual feedback after successful content generation.
 */
@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    circleColor: Color = Color(0xFF22C55E), // Green success color
    checkmarkColor: Color = Color(0xFF22C55E),
    strokeWidth: Dp = 4.dp,
    onAnimationComplete: () -> Unit = {}
) {
    // Animation progress values
    val circleProgress = remember { Animatable(0f) }
    val checkmarkProgress = remember { Animatable(0f) }
    val scaleProgress = remember { Animatable(0.5f) }
    
    LaunchedEffect(Unit) {
        // Scale in
        scaleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        )
        // Draw circle
        circleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        // Draw checkmark
        checkmarkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        onAnimationComplete()
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer(
                    scaleX = scaleProgress.value,
                    scaleY = scaleProgress.value
                )
        ) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (size.toPx() - strokeWidthPx) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            
            // Draw circle arc based on progress
            drawArc(
                color = circleColor,
                startAngle = -90f,
                sweepAngle = 360f * circleProgress.value,
                useCenter = false,
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = androidx.compose.ui.geometry.Size(
                    size.toPx() - strokeWidthPx,
                    size.toPx() - strokeWidthPx
                ),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // Draw checkmark based on progress
            if (checkmarkProgress.value > 0f) {
                val checkmarkSize = size.toPx() * 0.4f
                val offsetX = center.x - checkmarkSize * 0.3f
                val offsetY = center.y
                
                // First part of checkmark (short line)
                val firstLineProgress = (checkmarkProgress.value * 2).coerceAtMost(1f)
                val startPoint1 = Offset(offsetX - checkmarkSize * 0.2f, offsetY)
                val endPoint1 = Offset(
                    offsetX + checkmarkSize * 0.1f * firstLineProgress,
                    offsetY + checkmarkSize * 0.3f * firstLineProgress
                )
                
                if (firstLineProgress > 0f) {
                    drawLine(
                        color = checkmarkColor,
                        start = startPoint1,
                        end = endPoint1,
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
                
                // Second part of checkmark (long line)
                val secondLineProgress = ((checkmarkProgress.value - 0.5f) * 2).coerceIn(0f, 1f)
                if (secondLineProgress > 0f) {
                    val startPoint2 = Offset(offsetX + checkmarkSize * 0.1f, offsetY + checkmarkSize * 0.3f)
                    val endPoint2 = Offset(
                        offsetX + checkmarkSize * 0.1f + checkmarkSize * 0.5f * secondLineProgress,
                        offsetY + checkmarkSize * 0.3f - checkmarkSize * 0.5f * secondLineProgress
                    )
                    
                    drawLine(
                        color = checkmarkColor,
                        start = startPoint2,
                        end = endPoint2,
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
