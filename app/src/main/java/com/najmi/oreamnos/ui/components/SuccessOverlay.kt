package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A delightful success overlay with a particle explosion effect.
 * Triggers a celebratory animation when content generation succeeds.
 */
@Composable
fun SuccessOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)), // Dim background
            contentAlignment = Alignment.Center
        ) {
            // Particle Explosion
            if (visible) {
                ParticleExplosion()
            }

            // Animated Checkmark
            AnimatedCheckmark(
                size = 120.dp,
                circleColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 8.dp
            )
        }
    }
}

@Composable
private fun ParticleExplosion() {
    val particles = remember { List(20) { Particle() } }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = Modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.width / 2

        particles.forEach { particle ->
            val currentRadius = particle.initialRadius + (maxRadius - particle.initialRadius) * progress.value * particle.speed
            val alpha = (1f - progress.value).coerceIn(0f, 1f)

            val x = center.x + currentRadius * cos(particle.angle)
            val y = center.y + currentRadius * sin(particle.angle)

            drawCircle(
                color = (if (particle.isPrimary) primaryColor else secondaryColor).copy(alpha = alpha),
                radius = particle.size * (1f - progress.value * 0.5f), // Shrink slightly as they fly out
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}

private data class Particle(
    val angle: Double = Random.nextDouble(0.0, 2 * Math.PI),
    val speed: Float = Random.nextFloat() * 0.5f + 0.5f, // 0.5 to 1.0
    val size: Float = Random.nextFloat() * 10f + 5f, // 5 to 15
    val initialRadius: Float = Random.nextFloat() * 50f,
    val isPrimary: Boolean = Random.nextBoolean()
)
