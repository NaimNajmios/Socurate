package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A delightful full-screen overlay to celebrate success.
 * Features a particle explosion and an animated checkmark.
 */
@Composable
fun SuccessOverlay(
    visible: Boolean,
    message: String = "GENERATION COMPLETE",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = modifier.fillMaxSize()
    ) {
        // Semi-transparent scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            // Particle Explosion
            if (visible) {
                ParticleExplosion(
                    modifier = Modifier.matchParentSize()
                )
            }

            // Central Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedCheckmark(
                    size = 80.dp,
                    circleColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/**
 * A particle explosion animation using Canvas.
 */
@Composable
fun ParticleExplosion(
    modifier: Modifier = Modifier
) {
    val particles = remember { List(20) { Particle() } }
    val progress = remember { Animatable(0f) }
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutExpo)
        )
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)

        particles.forEach { particle ->
            val currentProgress = progress.value
            val alpha = (1f - currentProgress).coerceIn(0f, 1f)

            // Calculate position
            val angleRad = Math.toRadians(particle.angle.toDouble())
            val distance = particle.maxDistance * currentProgress
            val x = center.x + distance * cos(angleRad).toFloat()
            val y = center.y + distance * sin(angleRad).toFloat()

            // Calculate radius (grow then shrink)
            // Use dp to px conversion for consistency
            val maxRadiusPx = particle.maxRadius * 3f // Scaling factor for visibility
            val radius = maxRadiusPx * (1f - currentProgress)

            drawCircle(
                color = primaryColor.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

private class Particle {
    val angle = Random.nextFloat() * 360f
    val maxDistance = Random.nextFloat() * 300f + 100f // 100-400px travel
    val maxRadius = Random.nextFloat() * 10f + 5f
}
