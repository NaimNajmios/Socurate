package com.najmi.oreamnos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.model.UsageStats
import com.najmi.oreamnos.ui.theme.ErrorRed
import com.najmi.oreamnos.ui.theme.SuccessGreen
import java.util.Locale

/**
 * Success Rate Pie/Donut Chart
 * Shows success vs failure ratio visually with provider breakdown.
 */
@Composable
fun SuccessRateChart(
    stats: UsageStats,
    modifier: Modifier = Modifier
) {
    val successCount = stats.successfulRequests
    val failCount = stats.failedRequests
    val total = successCount + failCount
    val successRate = if (total > 0) successCount.toFloat() / total else 1f
    
    // Animation
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(1000),
        label = "donut_animation"
    )
    
    LaunchedEffect(stats) {
        animationProgress = 0f
        animationProgress = 1f
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Success Rate",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut chart
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val centerOffset = Offset(size.width / 2, size.height / 2)
                        
                        // Background track
                        drawCircle(
                            color = SuccessGreen.copy(alpha = 0.2f),
                            radius = radius,
                            center = centerOffset,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Success arc (green)
                        val successSweep = 360f * successRate * animatedProgress
                        drawArc(
                            color = SuccessGreen,
                            startAngle = -90f,
                            sweepAngle = successSweep,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        
                        // Failure arc (red)
                        if (failCount > 0) {
                            val failSweep = 360f * (1 - successRate) * animatedProgress
                            drawArc(
                                color = ErrorRed,
                                startAngle = -90f + successSweep,
                                sweepAngle = failSweep,
                                useCenter = false,
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    
                    // Center percentage text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", successRate * 100 * animatedProgress),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (successRate > 0.8f) SuccessGreen else if (successRate > 0.5f) MaterialTheme.colorScheme.onSurface else ErrorRed
                        )
                    }
                }
                
                // Stats
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow(label = "Successful", value = successCount.toString(), color = SuccessGreen)
                    StatRow(label = "Failed", value = failCount.toString(), color = ErrorRed)
                    StatRow(label = "Total", value = total.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            // Provider breakdown
            val providerRates = remember(stats) { stats.getSuccessRateByProvider() }
            if (providerRates.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "By Provider",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                
                providerRates.forEach { rate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = rate.provider.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f%% (%d/%d)", rate.rate, rate.successful, rate.successful + rate.failed),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (rate.rate > 80) SuccessGreen else if (rate.rate > 50) MaterialTheme.colorScheme.onSurfaceVariant else ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
