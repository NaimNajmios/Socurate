package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * Features a central checkmark and a particle explosion effect.
 *
 * Usage:
 * ```
 * SuccessOverlay(visible = showSuccess)
 * ```
 */
@Composable
fun SuccessOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    // Only render the overlay content if visible to save resources
    // The AnimatedVisibility in the parent/caller handles the enter/exit transitions
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Particle Explosion
            ExplosionEffect(visible)

            // Re-use existing AnimatedCheckmark
            // It will re-run its entrance animation whenever it enters the composition
            AnimatedCheckmark(
                size = 120.dp,
                circleColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 6.dp
            )
        }
    }
}

@Composable
private fun ExplosionEffect(trigger: Boolean) {
    // Generate random particle properties once
    val particles = remember { List(25) { Particle() } }

    // Create a transition that runs when 'trigger' changes
    val transition = updateTransition(targetState = trigger, label = "explosion")

    val progress by transition.animateFloat(
        transitionSpec = { tween(800, easing = LinearOutSlowInEasing) },
        label = "progress"
    ) { state ->
        if (state) 1f else 0f
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center

        particles.forEachIndexed { index, particle ->
            // Only draw if animation is active
            if (progress > 0f && progress < 1f) {
                val distance = particle.maxDistance * progress
                val x = center.x + cos(particle.angle) * distance
                val y = center.y + sin(particle.angle) * distance

                // Fade out as they move away
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val radius = particle.size * (1f - progress * 0.5f)

                // Alternate colors
                val color = if (index % 2 == 0) primaryColor else secondaryColor

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private data class Particle(
    val angle: Float = Random.nextFloat() * 2 * Math.PI.toFloat(),
    // Particles travel between 200 and 500 pixels
    val maxDistance: Float = Random.nextFloat() * 300f + 200f,
    // Size between 5 and 15 pixels
    val size: Float = Random.nextFloat() * 10f + 5f
)
