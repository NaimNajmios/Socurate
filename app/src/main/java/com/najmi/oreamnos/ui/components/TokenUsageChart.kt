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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.model.UsageStats
import com.najmi.oreamnos.ui.theme.SuccessGreen

/**
 * Token Usage Line Chart
 * Shows token usage trends over time with prompt vs response breakdown.
 */
@Composable
fun TokenUsageChart(
    stats: UsageStats,
    modifier: Modifier = Modifier
) {
    var selectedDays by remember { mutableIntStateOf(7) }
    val tokenData = remember(selectedDays, stats) { 
        stats.getTokenSplitPerDay(selectedDays) 
    }
    
    // Animation
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(1000),
        label = "chart_animation"
    )
    
    LaunchedEffect(tokenData) {
        animationProgress = 0f
        animationProgress = 1f
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with time range selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Token Usage Trends",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Time range chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30, 90).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("${days}d") }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (tokenData.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Chart
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = SuccessGreen
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val sortedData = tokenData.entries.sortedBy { it.key }
                    if (sortedData.isEmpty()) return@Canvas
                    
                    val maxValue = sortedData.maxOf { 
                        maxOf(it.value.promptTokens, it.value.responseTokens) 
                    }.toFloat().coerceAtLeast(1f)
                    
                    val stepX = size.width / (sortedData.size - 1).coerceAtLeast(1)
                    val padding = 8.dp.toPx()
                    val chartHeight = size.height - padding * 2
                    
                    // Draw prompt tokens line
                    val promptPath = Path()
                    sortedData.forEachIndexed { index, entry ->
                        val x = index * stepX
                        val y = padding + chartHeight * (1 - (entry.value.promptTokens / maxValue) * animatedProgress)
                        if (index == 0) {
                            promptPath.moveTo(x, y)
                        } else {
                            promptPath.lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = promptPath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Draw response tokens line
                    val responsePath = Path()
                    sortedData.forEachIndexed { index, entry ->
                        val x = index * stepX
                        val y = padding + chartHeight * (1 - (entry.value.responseTokens / maxValue) * animatedProgress)
                        if (index == 0) {
                            responsePath.moveTo(x, y)
                        } else {
                            responsePath.lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = responsePath,
                        color = secondaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Draw dots on data points
                    sortedData.forEachIndexed { index, entry ->
                        val x = index * stepX
                        val yPrompt = padding + chartHeight * (1 - (entry.value.promptTokens / maxValue) * animatedProgress)
                        val yResponse = padding + chartHeight * (1 - (entry.value.responseTokens / maxValue) * animatedProgress)
                        
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, yPrompt))
                        drawCircle(color = secondaryColor, radius = 4.dp.toPx(), center = Offset(x, yResponse))
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Prompt")
                Spacer(Modifier.width(24.dp))
                LegendItem(color = SuccessGreen, label = "Response")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
