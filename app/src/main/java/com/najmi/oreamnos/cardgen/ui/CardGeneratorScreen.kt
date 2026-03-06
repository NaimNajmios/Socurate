package com.najmi.oreamnos.cardgen.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.ExportSize
import com.najmi.oreamnos.cardgen.renderer.CardRenderer
import com.najmi.oreamnos.cardgen.utils.BitmapExporter
import com.najmi.oreamnos.cardgen.viewmodel.CardGeneratorViewModel
import com.najmi.oreamnos.cardgen.viewmodel.ExportState
import com.najmi.oreamnos.cardgen.viewmodel.ExtractionState
import com.najmi.oreamnos.ui.components.NeoButton
import com.najmi.oreamnos.ui.components.NeoOutlinedButton
import com.najmi.oreamnos.viewmodel.AppViewModel
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
    var selectedExportSize by remember { mutableStateOf(ExportSize.SQUARE) }

    // Consume piped text from AppViewModel (from the Generate screen)
    val pipedText by appViewModel.pipedArticleText.collectAsState()
    LaunchedEffect(pipedText) {
        if (pipedText.isNotBlank()) {
            cardViewModel.pipeFromMainFlow(pipedText, context)
            appViewModel.clearPipedText()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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

            // ── 3. Action bar ──────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoOutlinedButton(
                        text = "Background",
                        onClick = { showBackgroundSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                    NeoButton(
                        text = "Export",
                        onClick = {
                            if (extractionState is ExtractionState.Success) {
                                showExportSheet = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Generate card data first")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 4. Text input (direct paste) ───────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "ARTICLE TEXT",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(
                        2f, androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { cardViewModel.updateInputText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            text = "Paste a football article here...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    maxLines = 6,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                )
                Spacer(Modifier.height(8.dp))
                NeoButton(
                    text = "Extract Card Data",
                    onClick = { cardViewModel.extractCardData(context) },
                    isLoading = extractionState is ExtractionState.Loading,
                    enabled = inputText.isNotBlank() && extractionState !is ExtractionState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Sheets ─────────────────────────────────────────────────

    if (showBackgroundSheet) {
        BackgroundPickerSheet(
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
