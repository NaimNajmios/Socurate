package com.najmi.oreamnos.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.najmi.oreamnos.viewmodel.OcrViewModel

/**
 * Bottom sheet for importing player stats from screenshots via OCR.
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
                // Error handling handled by ViewModel or UI state
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
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "IMPORT FROM SCREENSHOT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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

            // Image Preview
            viewModel.selectedBitmap?.let { bitmap ->
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected screenshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Loading State
            if (viewModel.isLoading) {
                EnhancedLoadingCard(estimatedDurationMs = 3000L)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Error State
            viewModel.error?.let { err ->
                NeoCard(
                    borderColor = MaterialTheme.colorScheme.error,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column {
                        Text(
                            text = "OCR ERROR",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoButton(
                            onClick = { viewModel.retry() },
                            text = "RETRY",
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            /*
            // Extracted Text Field
            if (viewModel.extractedText.isNotEmpty() && !viewModel.isLoading) {
                Text(
                    text = "EXTRACTED TEXT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                NeoInput(
                    value = viewModel.extractedText,
                    onValueChange = { viewModel.extractedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            */

            // Confirm Button
            NeoButton(
                onClick = { 
                    onConfirm(viewModel.extractedText)
                    onDismiss()
                },
                text = "GENERATE POST",
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.extractedText.isNotEmpty() && !viewModel.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
