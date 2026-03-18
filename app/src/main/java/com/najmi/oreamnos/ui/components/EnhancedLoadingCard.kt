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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class LoadingType {
    GENERATING,
    EXTRACTING,
    DOWNLOADING,
    UPLOADING,
    ANALYZING,
    CUSTOM
}

private fun getLoadingConfig(type: LoadingType): Pair<ImageVector, String> {
    return when (type) {
        LoadingType.GENERATING -> Icons.Default.Psychology to "Generating content..."
        LoadingType.EXTRACTING -> Icons.Default.TextSnippet to "Extracting content..."
        LoadingType.DOWNLOADING -> Icons.Default.CloudDownload to "Downloading..."
        LoadingType.UPLOADING -> Icons.Default.Upload to "Uploading..."
        LoadingType.ANALYZING -> Icons.Default.Analytics to "Analyzing..."
        LoadingType.CUSTOM -> Icons.Default.Psychology to "Processing..."
    }
}

private fun getLoadingText(type: LoadingType, progress: Float): String {
    val prefix = when (type) {
        LoadingType.GENERATING -> "GENERATING"
        LoadingType.EXTRACTING -> "EXTRACTING"
        LoadingType.DOWNLOADING -> "DOWNLOADING"
        LoadingType.UPLOADING -> "UPLOADING"
        LoadingType.ANALYZING -> "ANALYZING"
        LoadingType.CUSTOM -> "PROCESSING"
    }
    return when {
        progress < 0.30f -> "$prefix..."
        progress < 0.60f -> "IN PROGRESS..."
        progress < 0.90f -> "ALMOST DONE..."
        else -> "COMPLETE!"
    }
}

/**
 * Enhanced Loading Card with Progress Percentage
 * Features:
 * - Larger, more prominent circular progress indicator
 * - Animated progress percentage (0% -> 100%)
 * - Dynamic loading text based on progress
 * - Pulsing animation effect
 * - Contextual loading types with icons
 */
@Composable
fun EnhancedLoadingCard(
    modifier: Modifier = Modifier,
    loadingType: LoadingType = LoadingType.GENERATING,
    customIcon: ImageVector? = null,
    estimatedDurationMs: Long = 8000L,
    progressFlow: kotlinx.coroutines.flow.StateFlow<Float>? = null,
    loadingMessage: String? = null
) {
    val (defaultIcon, defaultMessage) = getLoadingConfig(loadingType)
    val effectiveIcon = if (loadingType == LoadingType.CUSTOM && customIcon != null) customIcon else defaultIcon
    val effectiveMessage = loadingMessage ?: defaultMessage
    var simulatedProgress by remember { mutableFloatStateOf(0f) }
    
    val externalProgress = progressFlow?.collectAsState()
    val progress = externalProgress?.value ?: simulatedProgress
    
    val loadingText = if (progressFlow != null) {
        getLoadingText(loadingType, progress)
    } else {
        LaunchedEffect(Unit) {
            val startTime = System.currentTimeMillis()
            while (simulatedProgress < 0.95f) {
                delay(100)
                val elapsed = System.currentTimeMillis() - startTime
                simulatedProgress = (elapsed.toFloat() / estimatedDurationMs).coerceAtMost(0.95f)
            }
        }
        getLoadingText(loadingType, simulatedProgress)
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
            
            // Context icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = effectiveIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Subtitle hint
            Text(
                text = effectiveMessage,
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
    loadingType: LoadingType = LoadingType.GENERATING,
    customIcon: ImageVector? = null,
    isComplete: Boolean,
    onComplete: () -> Unit,
    progressFlow: kotlinx.coroutines.flow.StateFlow<Float>? = null,
    loadingMessage: String? = null
) {
    val (defaultIcon, defaultMessage) = getLoadingConfig(loadingType)
    val effectiveIcon = if (loadingType == LoadingType.CUSTOM && customIcon != null) customIcon else defaultIcon
    val effectiveMessage = loadingMessage ?: defaultMessage
    
    var simulatedProgress by remember { mutableFloatStateOf(0f) }
    
    val externalProgress = progressFlow?.collectAsState()
    val progress = externalProgress?.value ?: simulatedProgress
    
    val rawLoadingText = if (progressFlow != null) {
        getLoadingText(loadingType, progress)
    } else {
        LaunchedEffect(Unit) {
            val startTime = System.currentTimeMillis()
            while (simulatedProgress < 0.95f && !isComplete) {
                delay(100)
                val elapsed = System.currentTimeMillis() - startTime
                simulatedProgress = (elapsed.toFloat() / 8000f).coerceAtMost(0.95f)
            }
        }
        getLoadingText(loadingType, simulatedProgress)
    }
    
    val displayText = if (isComplete) "COMPLETE!" else rawLoadingText
    
    // When complete, animate to 100%
    LaunchedEffect(isComplete) {
        if (isComplete) {
            simulatedProgress = 1f
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = effectiveIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = effectiveMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
