package com.najmi.oreamnos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Enhanced Loading Card with Progress Percentage
 * Features:
 * - Larger, more prominent circular progress indicator
 * - Animated progress percentage (0% -> 100%)
 * - Dynamic loading text based on progress
 * - Pulsing animation effect
 */
@Composable
fun EnhancedLoadingCard(
    modifier: Modifier = Modifier,
    estimatedDurationMs: Long = 8000L // Default 8 seconds estimation
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var loadingText by remember { mutableStateOf("INITIALIZING...") }
    
    // Simulate progress over estimated time
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (progress < 0.95f) {
            delay(100)
            val elapsed = System.currentTimeMillis() - startTime
            // Use a curve that slows down as it approaches completion
            progress = (elapsed.toFloat() / estimatedDurationMs).coerceAtMost(0.95f)
            
            // Update loading text based on progress
            loadingText = when {
                progress < 0.30f -> "INITIALIZING..."
                progress < 0.60f -> "GENERATING..."
                progress < 0.90f -> "FINALIZING..."
                else -> "ALMOST DONE..."
            }
        }
    }
    
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(200),
        label = "progress"
    )
    
    NeoCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(pulseScale)
            ) {
                // Background track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    strokeWidth = 6.dp
                )
                
                // Progress indicator
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                
                // Percentage text in center
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Dynamic loading text
            Text(
                text = loadingText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Subtitle hint
            Text(
                text = "Creating your content...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Completes the progress animation when generation finishes
 */
@Composable
fun EnhancedLoadingCardWithCompletion(
    modifier: Modifier = Modifier,
    isComplete: Boolean,
    onComplete: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var loadingText by remember { mutableStateOf("INITIALIZING...") }
    
    // Simulate progress
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (progress < 0.95f && !isComplete) {
            delay(100)
            val elapsed = System.currentTimeMillis() - startTime
            progress = (elapsed.toFloat() / 8000f).coerceAtMost(0.95f)
            
            loadingText = when {
                progress < 0.30f -> "INITIALIZING..."
                progress < 0.60f -> "GENERATING..."
                progress < 0.90f -> "FINALIZING..."
                else -> "ALMOST DONE..."
            }
        }
    }
    
    // When complete, animate to 100%
    LaunchedEffect(isComplete) {
        if (isComplete) {
            progress = 1f
            loadingText = "COMPLETE!"
            delay(300)
            onComplete()
        }
    }
    
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(200),
        label = "progress"
    )
    
    NeoCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(pulseScale)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    strokeWidth = 6.dp
                )
                
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = loadingText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "Creating your content...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
