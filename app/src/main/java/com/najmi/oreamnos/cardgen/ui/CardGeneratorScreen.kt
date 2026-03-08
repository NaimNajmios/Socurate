package com.najmi.oreamnos.cardgen.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.ExportSize
import com.najmi.oreamnos.cardgen.model.ImagePosition
import com.najmi.oreamnos.cardgen.renderer.CardRenderer
import com.najmi.oreamnos.cardgen.utils.BitmapExporter
import com.najmi.oreamnos.cardgen.viewmodel.CardGeneratorViewModel
import com.najmi.oreamnos.cardgen.viewmodel.ExportState
import com.najmi.oreamnos.cardgen.viewmodel.ExtractionState
import com.najmi.oreamnos.ui.components.NeoButton
import com.najmi.oreamnos.ui.components.NeoOutlinedButton
import com.najmi.oreamnos.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch

/**
 * Main Card Generator screen.
 *
 * Layout (top-to-bottom):
 * 1. [TemplatePickerRow] — 4 template selector chips
 * 2. [CardPreviewPane] — live card preview
 * 3. Action bar — Change Background + Export buttons
 * 4. Optional text input (if user wants to type/paste directly)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardGeneratorScreen(
    appViewModel: AppViewModel,
    cardViewModel: CardGeneratorViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe states
    val inputText by cardViewModel.inputText.collectAsState()
    val selectedTemplate by cardViewModel.selectedTemplate.collectAsState()
    val extractionState by cardViewModel.extractionState.collectAsState()
    val cardConfig by cardViewModel.cardConfig.collectAsState()
    val exportState by cardViewModel.exportState.collectAsState()

    // Sheet visibility
    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showDataSheet by remember { mutableStateOf(false) }
    var selectedExportSize by remember { mutableStateOf(ExportSize.SQUARE) }

    // Consume synced text from AppViewModel (auto-sync from Generate screen)
    val latestText by appViewModel.latestGeneratedText.collectAsState()
    val hasUnconsumedText by appViewModel.hasUnconsumedText.collectAsState()
    
    LaunchedEffect(hasUnconsumedText, latestText) {
        if (hasUnconsumedText && latestText.isNotBlank()) {
            cardViewModel.consumeSyncedText(latestText, context)
            appViewModel.markTextConsumed()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Auto-filled with latest generated text",
                    withDismissAction = true
                )
            }
        }
    }

    // Show snackbar on extraction error
    LaunchedEffect(extractionState) {
        if (extractionState is ExtractionState.Error) {
            snackbarHostState.showSnackbar((extractionState as ExtractionState.Error).message)
        }
    }

    // Show snackbar on export error
    LaunchedEffect(exportState) {
        if (exportState is ExportState.Error) {
            snackbarHostState.showSnackbar((exportState as ExportState.Error).message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CARD",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (extractionState is ExtractionState.Success) {
                                showExportSheet = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Generate card data first")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NeoOutlinedButton(
                        text = "Data",
                        onClick = { showDataSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    NeoOutlinedButton(
                        text = "Design",
                        onClick = { showBackgroundSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Template picker ─────────────────────────────────
            TemplatePickerRow(
                selectedTemplate = selectedTemplate,
                onTemplateSelected = { template ->
                    cardViewModel.selectTemplate(template, context)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── 2. Card preview ────────────────────────────────────
            CardPreviewPane(
                extractionState = extractionState,
                selectedTemplate = selectedTemplate,
                cardConfig = cardConfig,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            // ── 3. Layout Options ──────────────────────────────────
            LayoutOptionsSection(
                cardConfig = cardConfig,
                onConfigUpdate = { cardViewModel.updateConfig(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Sheets ─────────────────────────────────────────────────

    if (showBackgroundSheet) {
        DesignBottomSheet(
            currentConfig = cardConfig,
            onConfigUpdate = { newConfig ->
                cardViewModel.updateConfig(newConfig)
                if (newConfig.backgroundBitmap != null) {
                    cardViewModel.setBackgroundBitmap(newConfig.backgroundBitmap)
                }
            },
            onDismiss = { showBackgroundSheet = false }
        )
    }

    if (showDataSheet) {
        DataEditorSheet(
            inputText = inputText,
            onInputTextChange = { cardViewModel.updateInputText(it) },
            isExtracting = extractionState is ExtractionState.Loading,
            onExtractClick = { cardViewModel.extractCardData(context) },
            onDismiss = { showDataSheet = false }
        )
    }

    if (showExportSheet) {
        ExportBottomSheet(
            exportState = exportState,
            selectedSize = selectedExportSize,
            onSizeSelected = { selectedExportSize = it },
            onSaveToGallery = {
                val currentState = extractionState
                if (currentState is ExtractionState.Success) {
                    scope.launch {
                        cardViewModel.setExportState(ExportState.Exporting)
                        try {
                            val bitmap = CardRenderer.renderToBitmap(
                                context = context,
                                cardData = currentState.cardData,
                                cardConfig = cardConfig,
                                exportSize = selectedExportSize
                            )
                            val uri = BitmapExporter.saveToGallery(bitmap, context)
                            cardViewModel.setExportState(ExportState.Saved(uri))
                        } catch (e: Exception) {
                            cardViewModel.setExportState(ExportState.Error(e.message ?: "Failed to save"))
                        }
                    }
                }
            },
            onShare = {
                val currentState = extractionState
                if (currentState is ExtractionState.Success) {
                    scope.launch {
                        cardViewModel.setExportState(ExportState.Exporting)
                        try {
                            val bitmap = CardRenderer.renderToBitmap(
                                context = context,
                                cardData = currentState.cardData,
                                cardConfig = cardConfig,
                                exportSize = selectedExportSize
                            )
                            BitmapExporter.shareImage(bitmap, context)
                            cardViewModel.setExportState(ExportState.Shared)
                        } catch (e: Exception) {
                            cardViewModel.setExportState(ExportState.Error(e.message ?: "Failed to share"))
                        }
                    }
                }
            },
            onDismiss = {
                showExportSheet = false
                cardViewModel.resetExportState()
            }
        )
    }
}

@Composable
private fun LayoutOptionsSection(
    cardConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Font Size Slider
        Text(
            text = "FONT SIZE",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Slider(
            value = cardConfig.fontSizeMultiplier,
            onValueChange = { onConfigUpdate(cardConfig.copy(fontSizeMultiplier = it)) },
            valueRange = 0.5f..2f,
            modifier = Modifier.fillMaxWidth()
        )

        // Image Position
        Text(
            text = "DESIGN LAYOUT",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        // A simple row of options using a LazyRow (horizontal scroll)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ImagePosition.entries.toList()) { position ->
                val isSelected = cardConfig.imagePosition == position
                Surface(
                    modifier = Modifier
                        .width(100.dp)
                        .height(48.dp)
                        .clickable { onConfigUpdate(cardConfig.copy(imagePosition = position)) },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.outline
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                           else MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = position.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Image Opacity
        if (cardConfig.backgroundBitmap != null || cardConfig.imagePosition == ImagePosition.MINIMAL) {
            Text(
                text = "IMAGE OPACITY: ${(cardConfig.imageOpacity * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Slider(
                value = cardConfig.imageOpacity,
                onValueChange = { onConfigUpdate(cardConfig.copy(imageOpacity = it)) },
                valueRange = 0.1f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
