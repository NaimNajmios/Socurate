package com.najmi.oreamnos.cardgen.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najmi.oreamnos.cardgen.extractor.CardDataExtractor
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.CardTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────
// State types
// ──────────────────────────────────────────────────────────────

/** UI state for the AI extraction process. */
sealed class ExtractionState {
    object Idle : ExtractionState()
    object Loading : ExtractionState()
    data class Success(val cardData: CardData) : ExtractionState()
    data class Error(val message: String) : ExtractionState()
}

/** UI state for the card export process. */
sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Saved(val uri: android.net.Uri) : ExportState()
    object Shared : ExportState()
    data class Error(val message: String) : ExportState()
}

// ──────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────

/**
 * ViewModel for [com.najmi.oreamnos.cardgen.ui.CardGeneratorScreen].
 *
 * Uses StateFlow throughout (unlike [com.najmi.oreamnos.viewmodel.MainViewModel] which
 * uses LiveData). This is the idiomatic pattern for pure Jetpack Compose screens.
 */
class CardGeneratorViewModel : ViewModel() {

    // ── Input ──────────────────────────────────────────────────

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // ── Template selection ─────────────────────────────────────

    private val _selectedTemplate = MutableStateFlow<CardTemplate>(CardTemplate.DetailedScoreboard)
    val selectedTemplate: StateFlow<CardTemplate> = _selectedTemplate.asStateFlow()

    // ── AI extraction state ────────────────────────────────────

    private val _extractionState = MutableStateFlow<ExtractionState>(ExtractionState.Idle)
    val extractionState: StateFlow<ExtractionState> = _extractionState.asStateFlow()

    // ── Dynamic Editable Data (For DataEditorSheet) ────────────

    private val _mutableCardData = MutableStateFlow<CardData?>(null)
    val mutableCardData: StateFlow<CardData?> = _mutableCardData.asStateFlow()

    // ── Card config (background, colors, size) ─────────────────

    private val _cardConfig = MutableStateFlow(CardConfig())
    val cardConfig: StateFlow<CardConfig> = _cardConfig.asStateFlow()

    // ── Background bitmap (from gallery pick) ─────────────────

    private val _backgroundBitmap = MutableStateFlow<Bitmap?>(null)
    val backgroundBitmap: StateFlow<Bitmap?> = _backgroundBitmap.asStateFlow()

    // ── Export state ───────────────────────────────────────────

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // ──────────────────────────────────────────────────────────────
    // Actions
    // ──────────────────────────────────────────────────────────────

    /**
     * Called when the user taps a template in [com.najmi.oreamnos.cardgen.ui.TemplatePickerRow].
     * Switches template and re-runs extraction if input text is present.
     */
    fun selectTemplate(template: CardTemplate, context: Context) {
        _selectedTemplate.value = template
        if (_inputText.value.isNotBlank()) {
            extractCardData(context)
        }
    }

    /**
     * Called when the user navigates to the Card screen, reading the synced text
     * from AppViewModel and automatically running extraction.
     */
    fun consumeSyncedText(text: String, context: Context) {
        if (text.isNotBlank() && text != _inputText.value) {
            _inputText.value = text
            extractCardData(context)
        }
    }

    /**
     * Updates the input text directly (when the user types into the field on the Card screen).
     */
    fun updateInputText(text: String) {
        _inputText.value = text
    }

    /**
     * Triggers AI extraction for the currently selected template.
     * Rate limit exceptions are caught and surfaced as [ExtractionState.Error].
     */
    fun extractCardData(context: Context, isRefresh: Boolean = false) {
        val text = _inputText.value
        if (text.isBlank()) return

        viewModelScope.launch {
            // Drop to Idle briefly to break StateFlow conflation if data returns identical
            if (isRefresh) {
                _extractionState.value = ExtractionState.Idle
                kotlinx.coroutines.delay(50)
            }
            
            _extractionState.value = ExtractionState.Loading
            kotlinx.coroutines.delay(100)
            try {
                val extractor = CardDataExtractor(context)
                val result = extractor.extract(_selectedTemplate.value, text, isRefresh)
                _extractionState.value = if (result.isSuccess) {
                    val data = result.getOrThrow()
                    _mutableCardData.value = data // Cache for live editing
                    ExtractionState.Success(data)
                } else {
                    ExtractionState.Error(result.exceptionOrNull()?.message ?: "Gagal mengekstrak data")
                }
            } catch (e: com.najmi.oreamnos.exceptions.RateLimitException) {
                // Surface as error state — the existing RateLimitDialog is shown at the
                // MainActivity level by observing the card screen's error rather than
                // bubbling the exception up. Future enhancement: wire full rate limit dialog.
                _extractionState.value = ExtractionState.Error(
                    "Had kadar dicapai. Sila cuba lagi dalam beberapa saat."
                )
            } catch (e: Exception) {
                _extractionState.value = ExtractionState.Error(
                    e.message ?: "Ralat tidak dijangka"
                )
            }
        }
    }

    /**
     * Updates the card configuration (background type, colors, size, etc.).
     */
    fun updateConfig(config: CardConfig) {
        _cardConfig.value = config
    }

    /**
     * Sets the background bitmap chosen from the gallery.
     * Automatically updates the CardConfig with the new bitmap and switches to GALLERY background type.
     */
    fun setBackgroundBitmap(bitmap: Bitmap?) {
        _backgroundBitmap.value = bitmap
        // Also update the card config reflect the new to bitmap
        if (bitmap != null) {
            _cardConfig.value = _cardConfig.value.copy(
                backgroundBitmap = bitmap,
                backgroundType = com.najmi.oreamnos.cardgen.model.BackgroundType.GALLERY,
                presetBackground = null
            )
        } else {
            _cardConfig.value = _cardConfig.value.copy(
                backgroundBitmap = null,
                backgroundType = com.najmi.oreamnos.cardgen.model.BackgroundType.GRADIENT
            )
        }
    }

    /**
     * Updates the image position/layout mode in the CardConfig.
     */
    fun setImagePosition(position: com.najmi.oreamnos.cardgen.model.ImagePosition) {
        _cardConfig.value = _cardConfig.value.copy(imagePosition = position)
    }

    /**
     * Updates the image opacity in the CardConfig.
     */
    fun setImageOpacity(opacity: Float) {
        _cardConfig.value = _cardConfig.value.copy(imageOpacity = opacity.coerceIn(0.1f, 1f))
    }

    /**
     * Updates the scrim settings in the CardConfig.
     */
    fun setScrimSettings(showScrim: Boolean, scrimType: com.najmi.oreamnos.cardgen.utils.GradientBuilder.ScrimType) {
        _cardConfig.value = _cardConfig.value.copy(
            showScrim = showScrim,
            scrimType = scrimType
        )
    }

    /**
     * Updates the cutout bitmap for CUTOUT mode (transparent PNG overlay).
     */
    fun setCutoutBitmap(bitmap: Bitmap?) {
        _cardConfig.value = _cardConfig.value.copy(cutoutBitmap = bitmap)
    }

    /**
     * Updates the export state — called by [com.najmi.oreamnos.cardgen.renderer.CardRenderer]
     * after saving/sharing.
     */
    fun setExportState(state: ExportState) {
        _exportState.value = state
    }

    /**
     * Resets the export state to Idle (e.g. when the export sheet is dismissed).
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    /**
     * Resets the extraction state to Idle (e.g. when input text is cleared).
     */
    fun resetExtractionState() {
        _extractionState.value = ExtractionState.Idle
    }

    // ──────────────────────────────────────────────────────────────
    // Inline Data Editing Functions
    // ──────────────────────────────────────────────────────────────

    /**
     * Updates the actively cached [CardData]. 
     * Applies the lambda modification and re-emits to update the canvas.
     */
    fun updateCardData(updater: (CardData) -> CardData) {
        val currentData = _mutableCardData.value ?: return
        val updatedData = updater(currentData)
        _mutableCardData.value = updatedData
        // Re-emit into the primary pipeline so CardPreviewPane recomposes 
        _extractionState.value = ExtractionState.Success(updatedData)
    }
}
