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
 */
@Composable
fun VisionModelDownloadCard(
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: (VisionModel) -> Unit,
    onSkipClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedModel by remember { mutableStateOf(VisionModel.PALIGEMMA_2_3B) }

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
                    text = "Extract player stats and match data directly from screenshots — no OCR step needed. One-time download, fully offline after.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Gemini Nano status (informational)
                Text(
                    text = "• Gemini Nano: Not supported on this device",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select a model to download:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    ModelChoiceChip(
                        model = VisionModel.PALIGEMMA_2_3B,
                        selected = selectedModel == VisionModel.PALIGEMMA_2_3B,
                        onClick = { selectedModel = VisionModel.PALIGEMMA_2_3B },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ModelChoiceChip(
                        model = VisionModel.GEMMA_3_4B,
                        selected = selectedModel == VisionModel.GEMMA_3_4B,
                        onClick = { selectedModel = VisionModel.GEMMA_3_4B },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                NeoButton(
                    onClick = { onDownloadClick(selectedModel) },
                    text = "DOWNLOAD SELECTED MODEL",
                    modifier = Modifier.fillMaxWidth()
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
                    text = "DOWNLOADING ${selectedModel.displayName}...",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
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
                    Text(
                        text = "${(selectedModel.approximateSizeMb * downloadProgress).toInt()} MB of ${selectedModel.approximateSizeMb} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
private fun ModelChoiceChip(
    model: VisionModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeoChip(
        text = "${model.displayName}\n~${model.approximateSizeMb / 1000.0}GB\n${if (model == VisionModel.PALIGEMMA_2_3B) "Mid-range" else "Flagship"}",
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}
