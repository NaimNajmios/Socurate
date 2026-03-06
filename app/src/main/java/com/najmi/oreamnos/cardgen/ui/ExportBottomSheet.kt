package com.najmi.oreamnos.cardgen.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.cardgen.model.ExportSize
import com.najmi.oreamnos.cardgen.viewmodel.ExportState
import com.najmi.oreamnos.ui.components.AnimatedCheckmark
import com.najmi.oreamnos.ui.components.NeoButton
import com.najmi.oreamnos.ui.components.NeoChip
import com.najmi.oreamnos.ui.components.NeoOutlinedButton

/**
 * Bottom sheet for exporting the card.
 * Shows size selector chips (Square / Portrait / Story), Save and Share buttons.
 * Shows [AnimatedCheckmark] after a successful save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    exportState: ExportState,
    selectedSize: ExportSize,
    onSizeSelected: (ExportSize) -> Unit,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "EXPORT CARD",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Choose size, then save or share",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))

            // Size chips
            Text(
                text = "SIZE",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ExportSize.entries.forEach { size ->
                    NeoChip(
                        text = size.label,
                        selected = selectedSize == size,
                        onClick = { onSizeSelected(size) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action buttons / success state
            AnimatedContent(
                targetState = exportState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "exportContent"
            ) { state ->
                when (state) {
                    is ExportState.Idle, is ExportState.Error -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (state is ExportState.Error) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            NeoButton(
                                text = "Save to Gallery",
                                onClick = onSaveToGallery,
                                modifier = Modifier.fillMaxWidth()
                            )
                            NeoOutlinedButton(
                                text = "Share",
                                onClick = onShare,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    is ExportState.Exporting -> {
                        NeoButton(
                            text = "Saving...",
                            onClick = {},
                            isLoading = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is ExportState.Saved, is ExportState.Shared -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnimatedCheckmark(
                                size = 64.dp,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (state is ExportState.Saved) "Saved!" else "Shared!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
