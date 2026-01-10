package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A full-screen overlay that displays a delightful success animation.
 * Consists of a central animated checkmark and a particle explosion effect.
 *
 * @param visible Whether the overlay is visible.
 */
@Composable
fun SuccessOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)), // Blur-like overlay
            contentAlignment = Alignment.Center
        ) {
            // Particle Explosion
            ParticleExplosion(isVisible = visible)

            // Central Checkmark
            AnimatedCheckmark(
                size = 100.dp, // Large and prominent
                circleColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )
        }
    }
}

@Composable
private fun ParticleExplosion(isVisible: Boolean) {
    // Create stable particles
    val particles = remember { List(25) { Particle() } }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        // Explosion radius relative to screen size
        val maxRadius = size.minDimension * 0.4f

        particles.forEach { particle ->
            val currentRadius = maxRadius * progress.value * particle.speed
            val x = center.x + cos(particle.angle).toFloat() * currentRadius
            val y = center.y + sin(particle.angle).toFloat() * currentRadius

            // Fade out as they move away
            val alpha = (1f - progress.value).coerceIn(0f, 1f)

            drawCircle(
                color = (if (particle.isPrimary) primaryColor else secondaryColor).copy(alpha = alpha),
                radius = particle.size * (1f - progress.value * 0.3f), // Slight shrink
                center = Offset(x, y)
            )
        }
    }
}

private data class Particle(
    val angle: Double = Random.nextDouble(0.0, 2 * Math.PI),
    val speed: Float = Random.nextDouble(0.5, 1.2).toFloat(),
    val size: Float = Random.nextDouble(8.0, 16.0).toFloat(),
    val isPrimary: Boolean = Random.nextBoolean()
)
