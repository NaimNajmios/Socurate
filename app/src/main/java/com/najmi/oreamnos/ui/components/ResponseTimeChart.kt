package com.najmi.oreamnos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.model.UsageStats
import com.najmi.oreamnos.ui.theme.SuccessGreen
import java.util.Locale

/**
 * Response Time Bar Chart
 * Shows average response time per provider with highlights for fastest/slowest.
 */
@Composable
fun ResponseTimeChart(
    stats: UsageStats,
    modifier: Modifier = Modifier
) {
    val responseTimes = remember(stats) { stats.getAverageResponseTimeByProvider() }
    val fastest = remember(stats) { stats.getFastestGeneration() }
    val slowest = remember(stats) { stats.getSlowestGeneration() }
    
    // Animation
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(800),
        label = "bar_animation"
    )
    
    LaunchedEffect(stats) {
        animationProgress = 0f
        animationProgress = 1f
    }
    
    // Provider colors
    val providerColors = mapOf(
        "gemini" to MaterialTheme.colorScheme.primary,
        "groq" to Color(0xFFFF6B6B),
        "openrouter" to Color(0xFF4ECDC4),
        "cerebras" to Color(0xFF9C27B0)
    )
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Response Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (responseTimes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No response time data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Bar chart
                val maxTime = responseTimes.maxOfOrNull { it.averageMs }?.toFloat() ?: 1f
                
                responseTimes.forEach { providerTime ->
                    val barColor = providerColors[providerTime.provider] ?: MaterialTheme.colorScheme.primary
                    val barProgress = (providerTime.averageMs / maxTime) * animatedProgress
                    
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = providerTime.provider.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatDuration(providerTime.averageMs),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = barColor
                            )
                        }
                        
                        Spacer(Modifier.height(4.dp))
                        
                        // Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset.Zero,
                                    size = Size(size.width * barProgress, size.height),
                                    cornerRadius = CornerRadius(4.dp.toPx())
                                )
                            }
                        }
                        
                        // Min/Max info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Min: ${formatDuration(providerTime.minMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${providerTime.requestCount} requests",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Max: ${formatDuration(providerTime.maxMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Fastest/Slowest highlights
                if (fastest != null || slowest != null) {
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        fastest?.let {
                            HighlightBadge(
                                label = "⚡ Fastest",
                                value = formatDuration(it.durationMs),
                                provider = it.provider ?: "Unknown",
                                color = SuccessGreen
                            )
                        }
                        
                        slowest?.let {
                            HighlightBadge(
                                label = "🐢 Slowest",
                                value = formatDuration(it.durationMs),
                                provider = it.provider ?: "Unknown",
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightBadge(
    label: String,
    value: String,
    provider: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = provider.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(ms: Long): String {
    return when {
        ms < 1000 -> "${ms}ms"
        ms < 60000 -> String.format(Locale.US, "%.1fs", ms / 1000.0)
        else -> String.format(Locale.US, "%.1fm", ms / 60000.0)
    }
}
