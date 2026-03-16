package com.najmi.oreamnos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.vision.VisionModel

/**
 * Card shown when a vision model needs to be downloaded.
 * Shows options for Gemma 3n E2B (recommended), Gemma 3 1B (lighter), and PaliGemma.
 * 
 * Note: Full LiteRT integration requires Kotlin 2.0+.
 */
@Composable
fun VisionModelDownloadCard(
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadedMb: Long,
    totalMb: Long,
    selectedModel: VisionModel,
    onModelSelect: (VisionModel) -> Unit,
    onDownloadClick: (VisionModel) -> Unit,
    onSkipClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Default to Gemma 3n E2B as recommended
    var currentSelected by remember { 
        mutableStateOf(VisionModel.GEMMA_3N_E2B) 
    }

    NeoCard(
        modifier = modifier.animateContentSize(),
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!isDownloading) {
                Text(
                    text = "UPGRADE TO VISION AI",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Extract player stats and match data directly from screenshots. One-time download, fully offline after.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Info about coming soon
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Model options - showing as coming soon
                val availableModels = listOf(
                    VisionModel.GEMMA_3N_E2B,
                    VisionModel.GEMMA_3_1B,
                    VisionModel.PALIGEMMA_3B
                )

                availableModels.forEach { model ->
                    ModelOptionRow(
                        model = model,
                        selected = currentSelected == model,
                        onClick = { 
                            currentSelected = model
                            onModelSelect(model)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Note about Kotlin version
                Text(
                    text = "Requires Kotlin 2.0+ (coming in next update)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                NeoButton(
                    onClick = { onDownloadClick(currentSelected) },
                    text = "NOTIFY WHEN AVAILABLE",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onSkipClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "USE OCR INSTEAD",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = "DOWNLOADING ${selectedModel.displayName.toUpperCase()}...",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (totalMb > 0) {
                        Text(
                            text = "$downloadedMb MB of $totalMb MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                NeoButton(
                    onClick = onCancelClick,
                    text = "CANCEL",
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModelOptionRow(
    model: VisionModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayInfo = when (model) {
        VisionModel.GEMMA_3N_E2B -> "Gemma 3n E2B" to "~2.9GB - Best for screenshots (multimodal)"
        VisionModel.GEMMA_3_1B -> "Gemma 3 1B" to "~557MB - Fast, uses OCR first"
        VisionModel.PALIGEMMA_3B -> "PaliGemma 3B" to "~3GB - Legacy option"
        else -> model.displayName to "~${model.approximateSizeMb}MB"
    }
    
    NeoChip(
        text = "${displayInfo.first}\n${displayInfo.second}",
        selected = selected,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    )
}
