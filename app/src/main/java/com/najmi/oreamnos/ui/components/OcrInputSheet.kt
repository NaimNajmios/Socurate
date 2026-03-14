package com.najmi.oreamnos.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.viewmodel.OcrViewModel
import com.najmi.oreamnos.vision.VisionModel

/**
 * Bottom sheet for importing player stats from screenshots via Vision AI or OCR.
 * Implements a 7-state UI machine.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrInputSheet(
    viewModel: OcrViewModel,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val state = viewModel.uiState
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    bitmap?.let { viewModel.onImageSelected(it) }
                }
            } catch (e: Exception) {
                // Handled in VM
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState)
                .animateContentSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "IMPORT FROM SCREENSHOT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Image Source Row
            NeoChip(
                text = "IMPORT FROM GALLERY",
                selected = false,
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. IDLE / PICKED IMAGE PREVIEW
            state.selectedBitmap?.let { bitmap ->
                val thumbnailHeight = if (state.editableText.isNotEmpty()) 100.dp else 200.dp
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbnailHeight)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected screenshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (state.editableText.isEmpty()) {
                    TextButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CHANGE IMAGE", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } ?: run {
                // Idle state: Drop zone
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Tap Gallery above to select a screenshot",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. MODEL DOWNLOAD CARD (Conditional)
            val showDownloadCard = !state.geminiNanoAvailable && 
                                  state.installedMediaPipeModels.isEmpty() && 
                                  !state.isExtracting && 
                                  state.extractionResult == null

            if (showDownloadCard || state.isModelDownloading) {
                VisionModelDownloadCard(
                    isDownloading = state.isModelDownloading,
                    downloadProgress = state.modelDownloadProgress,
                    onDownloadClick = { viewModel.startDownload(it) },
                    onSkipClick = { /* User just proceeds with OCR */ },
                    onCancelClick = { viewModel.cancelDownload() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. EXTRACTING (Loading)
            if (state.isExtracting) {
                val loadingLabel = when (state.activeExtractorModel) {
                    VisionModel.GEMINI_NANO -> "Analysing with On-Device OCR…"
                    VisionModel.PALIGEMMA_2_3B -> "Analysing with PaliGemma…"
                    VisionModel.GEMMA_3_4B -> "Analysing with Gemma 3…"
                    VisionModel.ML_KIT -> "Reading image…"
                }
                
                EnhancedLoadingCard(
                    modifier = Modifier.fillMaxWidth(),
                    // Vision models take longer
                    estimatedDurationMs = if (state.activeExtractorModel == VisionModel.ML_KIT) 3000L else 12000L
                )
                Text(
                    text = loadingLabel,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. EXTRACTION RESULT
            AnimatedVisibility(visible = state.editableText.isNotEmpty() && !state.isExtracting) {
                Column {
                    state.extractionResult?.let { result ->
                        ExtractionSourceBadge(
                            model = result.source,
                            durationMs = result.durationMs,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (state.showFallbackNotice) {
                        FallbackNotificationCard(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    NeoInput(
                        value = state.editableText,
                        onValueChange = { viewModel.updateEditableText(it) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10,
                        placeholder = "Extracted data will appear here..."
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 5. ERROR STATE
            state.error?.let { err ->
                NeoCard(
                    borderColor = MaterialTheme.colorScheme.error,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EXTRACTION ERROR",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NeoButton(
                            onClick = { viewModel.retry() },
                            text = "TRY AGAIN",
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 6. CONFIRM BUTTON
            NeoButton(
                onClick = { 
                    onConfirm(state.editableText)
                    onDismiss()
                },
                text = "USE THIS TEXT",
                modifier = Modifier.fillMaxWidth(),
                enabled = state.editableText.isNotEmpty() && !state.isExtracting
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
