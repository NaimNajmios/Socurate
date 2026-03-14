package com.najmi.oreamnos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.vision.VisionModel

/**
 * Badge showing which extraction source was used with a learning tooltip.
 */
@Composable
fun ExtractionSourceBadge(
    model: VisionModel,
    durationMs: Long?,
    modifier: Modifier = Modifier
) {
    var showTooltip by remember { mutableStateOf(false) }

    val badgeLabel = when (model) {
        VisionModel.GEMINI_NANO -> "✓ On-Device OCR"
        VisionModel.PALIGEMMA_2_3B -> "✓ PaliGemma Vision"
        VisionModel.GEMMA_3_4B -> "✓ Gemma 3 Vision"
        VisionModel.ML_KIT -> "OCR Fallback"
    }

    val isVision = model != VisionModel.ML_KIT

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeoChip(
                text = badgeLabel,
                selected = isVision,
                onClick = { showTooltip = !showTooltip },
                modifier = Modifier.wrapContentSize()
            )

            if (isVision && durationMs != null) {
                NeoChip(
                    text = "Extracted in ${"%.1f".format(durationMs / 1000.0)}s",
                    selected = false,
                    onClick = { },
                    modifier = Modifier.wrapContentSize()
                )
            }
        }

        if (showTooltip) {
            Spacer(modifier = Modifier.height(8.dp))
            NeoCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = getTooltipText(model, durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

private fun getTooltipText(model: VisionModel, durationMs: Long?): String {
    val durationText = if (durationMs != null) " Extracted in ${"%.1f".format(durationMs / 1000.0)}s." else ""
    return when (model) {
        VisionModel.GEMINI_NANO -> "Extracted using on-device OCR with ML Kit. Text is then intelligently structured using AI.$durationText"
        VisionModel.PALIGEMMA_2_3B -> "Extracted using PaliGemma 2 3B running fully offline on your device.$durationText"
        VisionModel.GEMMA_3_4B -> "Extracted using Gemma 3 4B running fully offline on your device.$durationText"
        VisionModel.ML_KIT -> "Extracted using ML Kit OCR. Text only — no AI interpretation. For better results, use the on-device OCR option."
    }
}
