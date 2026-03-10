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
import kotlin.random.Random
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import com.najmi.oreamnos.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val _rewritingFields = MutableStateFlow<Set<String>>(emptySet())
    val rewritingFields: StateFlow<Set<String>> = _rewritingFields.asStateFlow()

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
     * If data has already been extracted, attempts a "hot swap" mapping instead of a full re-extraction.
     */
    fun selectTemplate(template: CardTemplate, context: Context) {
        if (_selectedTemplate.value == template) return
        
        _selectedTemplate.value = template
        
        val currentData = _mutableCardData.value
        if (currentData != null && _extractionState.value is ExtractionState.Success) {
            // Hot swap!
            swapTemplateAndMapData(currentData, template)
        } else if (_inputText.value.isNotBlank()) {
            extractCardData(context)
        }
    }

    /**
     * Intelligently maps the existing [CardData] fields into a new [CardTemplate]
     * to prevent data loss when switching templates post-extraction.
     */
    private fun swapTemplateAndMapData(currentData: CardData, targetTemplate: CardTemplate) {
        val newData: CardData = when (targetTemplate) {
            CardTemplate.HeadlineQuote -> CardData.HeadlineQuote(
                headline = findMostRelevantHeadline(currentData),
                subtext = "",
                quoteAuthor = ""
            )
            CardTemplate.PlayerSpotlight -> CardData.PlayerSpotlight(
                playerName = extractPlayerName(currentData),
                club = extractHomeOrAwayTeam(currentData),
                position = "FW",
                rating = 0f,
                goals = 0,
                assists = 0,
                minutesPlayed = 90,
                keyAction = "",
                keyQuote = findMostRelevantHeadline(currentData)
            )
            CardTemplate.DetailedScoreboard -> CardData.DetailedScoreboard(
                homeTeam = extractHomeTeam(currentData),
                awayTeam = extractAwayTeam(currentData),
                homeScore = 0,
                awayScore = 0,
                homeScorers = "",
                awayScorers = "",
                possession = "50% - 50%",
                shotsOnTarget = "0 - 0",
                competition = extractCompetition(currentData),
                matchStatus = "FT"
            )
            CardTemplate.MatchPreview -> CardData.MatchPreview(
                competition = extractCompetition(currentData),
                homeTeam = extractHomeTeam(currentData),
                awayTeam = extractAwayTeam(currentData),
                homeForm = "W W W",
                awayForm = "D L W",
                matchTime = "TBC",
                stadium = "TBC"
            )
            CardTemplate.TransferNews -> CardData.TransferNews(
                playerName = extractPlayerName(currentData),
                action = "SIGNED",
                fromTeam = extractAwayTeam(currentData),
                toTeam = extractHomeTeam(currentData),
                fee = "Undisclosed",
                contractLength = "Undisclosed",
                transferType = "Permanent",
                quote = findMostRelevantHeadline(currentData)
            )
            CardTemplate.TopStats -> CardData.TopStats(
                matchContext = extractCompetition(currentData),
                stats = listOf(
                    com.najmi.oreamnos.cardgen.model.StatItem("Stat 1", "-", ""),
                    com.najmi.oreamnos.cardgen.model.StatItem("Stat 2", "-", ""),
                    com.najmi.oreamnos.cardgen.model.StatItem("Stat 3", "-", "")
                )
            )
            CardTemplate.BreakingNews -> CardData.BreakingNews(
                label = "BREAKING",
                headline = findMostRelevantHeadline(currentData),
                subtext = "",
                impactRating = 5,
                relatedTeams = extractHomeOrAwayTeam(currentData)
            )
            CardTemplate.OnThisDay -> CardData.OnThisDay(
                dateLabel = "Today",
                yearsAgo = 1,
                competition = extractCompetition(currentData),
                headline = findMostRelevantHeadline(currentData),
                keyStats = emptyList()
            )
            CardTemplate.StartingXI -> CardData.StartingXI(
                teamName = extractHomeTeam(currentData),
                formation = "4-3-3",
                manager = "Manager",
                averageAge = "25.0",
                keyAbsences = "None",
                starters = emptyList(),
                subs = emptyList()
            )
        }
        
        _mutableCardData.value = newData
        _extractionState.value = ExtractionState.Success(newData)
    }

    // --- Helper extraction methods for mapping ---
    private fun findMostRelevantHeadline(currentData: CardData): String = when (currentData) {
        is CardData.HeadlineQuote -> currentData.headline
        is CardData.BreakingNews -> currentData.headline
        is CardData.OnThisDay -> currentData.headline
        is CardData.PlayerSpotlight -> currentData.keyQuote ?: ""
        is CardData.TransferNews -> "${currentData.playerName} Transfers to ${currentData.toTeam}"
        is CardData.DetailedScoreboard -> "${currentData.homeTeam} vs ${currentData.awayTeam}"
        is CardData.MatchPreview -> "${currentData.homeTeam} vs ${currentData.awayTeam} Preview"
        else -> ""
    }
    
    private fun extractPlayerName(currentData: CardData): String = when (currentData) {
        is CardData.PlayerSpotlight -> currentData.playerName
        is CardData.TransferNews -> currentData.playerName
        is CardData.HeadlineQuote -> currentData.quoteAuthor ?: ""
        else -> "Player Name"
    }

    private fun extractHomeTeam(currentData: CardData): String = when (currentData) {
        is CardData.DetailedScoreboard -> currentData.homeTeam
        is CardData.MatchPreview -> currentData.homeTeam
        is CardData.StartingXI -> currentData.teamName
        is CardData.TransferNews -> currentData.toTeam
        is CardData.PlayerSpotlight -> currentData.club
        else -> "Home Team"
    }

    private fun extractAwayTeam(currentData: CardData): String = when (currentData) {
        is CardData.DetailedScoreboard -> currentData.awayTeam
        is CardData.MatchPreview -> currentData.awayTeam
        is CardData.TransferNews -> currentData.fromTeam
        else -> "Away Team"
    }
    
    private fun extractHomeOrAwayTeam(currentData: CardData): String {
        val home = extractHomeTeam(currentData)
        return if (home != "Home Team") home else extractAwayTeam(currentData)
    }

    private fun extractCompetition(currentData: CardData): String = when (currentData) {
        is CardData.DetailedScoreboard -> currentData.competition
        is CardData.MatchPreview -> currentData.competition
        is CardData.TopStats -> currentData.matchContext
        else -> "League"
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
                    
                    // Smart Template Suggestion logic:
                    // If the AI suggests a different template and it's not a manual refresh,
                    // we switch to that template.
                    val suggested = data.suggestedTemplate
                    if (suggested != null && suggested != _selectedTemplate.value && !isRefresh) {
                        _selectedTemplate.value = suggested
                        // Note: In a production app, we might want to trigger a second extraction
                        // here with the correct template schema, but for now we'll switch
                        // to the layout and let the user see the suggested fit.
                    }
                    
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
     * Re-writes a specific text field using the Gemini AI API.
     */
    fun rewriteField(context: Context, fieldLabel: String, currentText: String, onUpdate: (String) -> Unit) {
        if (currentText.isBlank()) return
        
        viewModelScope.launch {
            _rewritingFields.value = _rewritingFields.value + fieldLabel
            try {
                val curator = com.najmi.oreamnos.curator.CuratorFactory.create(context)
                val prompt = """
                    You are a professional sports media copywriter. 
                    Rewrite the following football text to be punchier, more professional, and concise for social media.
                    Ensure the output is in Bahasa Malaysia.
                    Do NOT include any greetings, explanations, or quotes around the output. 
                    Output ONLY the refined text.
                    
                    Text: $currentText
                """.trimIndent()
                
                val result = curator.generateRaw(prompt).trim('"', '\'', ' ', '\n')
                if (result.isNotBlank()) {
                    onUpdate(result)
                }
            } catch (e: Exception) {
                _extractionState.value = ExtractionState.Error(
                    "Gagal menulis semula teks: ${e.message}"
                )
            } finally {
                _rewritingFields.value = _rewritingFields.value - fieldLabel
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
     * Sets the watermark URI and decodes it into a bitmap for overlay rendering.
     */
    fun setWatermarkUri(context: Context, uri: android.net.Uri?) {
        if (uri == null) {
            _cardConfig.value = _cardConfig.value.copy(
                watermarkUri = null,
                watermarkBitmap = null
            )
            return
        }

        viewModelScope.launch {
            try {
                // Wrap in software copy to avoid hardware bitmap issues during export
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        .copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                }
                
                _cardConfig.value = _cardConfig.value.copy(
                    watermarkUri = uri.toString(),
                    watermarkBitmap = bitmap
                )

                // Persist this watermark for future use
                saveBitmapToInternalStorage(context, bitmap)?.let { path ->
                    PreferencesManager(context).saveWatermarkPath(path)
                }
            } catch (e: Exception) {
                _extractionState.value = ExtractionState.Error("Gagal memuat watermark: ${e.message}")
            }
        }
    }

    /**
     * Clears the watermark from the current session and persistent storage.
     */
    fun clearWatermark(context: Context) {
        _cardConfig.value = _cardConfig.value.copy(
            watermarkUri = null,
            watermarkBitmap = null
        )
        val prefs = PreferencesManager(context)
        prefs.getWatermarkPath()?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                // Ignore delete errors
            }
        }
        prefs.saveWatermarkPath(null)
    }

    /**
     * Loads the persistent watermark from internal storage if it exists.
     */
    fun loadPersistentWatermark(context: Context) {
        val path = PreferencesManager(context).getWatermarkPath() ?: return
        
        viewModelScope.launch {
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(path)
                    }
                    if (bitmap != null) {
                        _cardConfig.value = _cardConfig.value.copy(
                            watermarkUri = android.net.Uri.fromFile(file).toString(),
                            watermarkBitmap = bitmap
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently fail if persistent watermark cannot be loaded
            }
        }
    }

    /**
     * Saves a bitmap to the app's internal files directory.
     */
    private suspend fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            try {
                val directory = File(context.filesDir, "watermarks")
                if (!directory.exists()) directory.mkdirs()
                
                val file = File(directory, "default_watermark.png")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                file.absolutePath
            } catch (e: Exception) {
                null
            }
        }
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

    // ──────────────────────────────────────────────────────────────
    // Workflow Enhancements
    // ──────────────────────────────────────────────────────────────

    /**
     * Re-rolls the card's visual design settings (opacity, fonts)
     * as part of the "Surprise Me" feature.
     */
    fun shuffleDesign() {
        val availableFonts = listOf(
            null, // Default
            "Serif",
            "Monospace"
        )
        val availableScrims = listOf(
            com.najmi.oreamnos.cardgen.utils.GradientBuilder.ScrimType.DARK,
            com.najmi.oreamnos.cardgen.utils.GradientBuilder.ScrimType.LIGHT,
            com.najmi.oreamnos.cardgen.utils.GradientBuilder.ScrimType.HORIZONTAL,
            com.najmi.oreamnos.cardgen.utils.GradientBuilder.ScrimType.REVERSE_HORIZONTAL,
            com.najmi.oreamnos.cardgen.utils.GradientBuilder.ScrimType.MINIMAL
        )

        val newConfig = _cardConfig.value.copy(
            primaryFontFamilyName = availableFonts.random(),
            imagePosition = com.najmi.oreamnos.cardgen.model.ImagePosition.entries.random(),
            overlayOpacity = Random.nextFloat() * (0.8f - 0.2f) + 0.2f, // 0.2f to 0.8f
            scrimType = availableScrims.random(),
            photoFilter = com.najmi.oreamnos.cardgen.model.PhotoFilter.entries.random(),
            textShadowRadius = Random.nextFloat() * 12f,
            isGlowEnabled = Random.nextBoolean()
        )
        
        _cardConfig.value = newConfig
    }
}
