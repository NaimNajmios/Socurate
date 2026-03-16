package com.najmi.oreamnos.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najmi.oreamnos.utils.FootballOcrParser
import com.najmi.oreamnos.utils.OcrUtils
import com.najmi.oreamnos.model.VisionExtractionResult
import com.najmi.oreamnos.utils.VisionModelManager
import com.najmi.oreamnos.vision.IVisionExtractor
import com.najmi.oreamnos.vision.VisionExtractorFactory
import com.najmi.oreamnos.vision.VisionModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Enhanced UI State for the vision-powered OCR flow.
 */
data class OcrUiState(
    val selectedBitmap: Bitmap? = null,
    val isExtracting: Boolean = false,
    val editableText: String = "",
    val extractionResult: VisionExtractionResult? = null,
    val isModelDownloading: Boolean = false,
    val modelDownloadProgress: Float = 0f,
    val activeExtractorModel: VisionModel = VisionModel.ML_KIT,
    val geminiNanoAvailable: Boolean = false,
    val installedMediaPipeModels: List<VisionModel> = emptyList(),
    val error: String? = null,
    val showFallbackNotice: Boolean = false
)

/**
 * ViewModel for the multi-path Vision Extraction flow.
 * Extends AndroidViewModel to access application context for model management.
 */
class OcrViewModel(application: Application) : AndroidViewModel(application) {

    private val modelManager = VisionModelManager(application)
    private val extractorFactory = VisionExtractorFactory(application, modelManager)
    private var activeExtractor: IVisionExtractor? = null
    private var downloadJob: Job? = null

    // UI state using the enhanced data class pattern
    var uiState by mutableStateOf(OcrUiState())
        private set

    init {
        // Hydrate initial state
        refreshModelState()
    }

    private fun refreshModelState() {
        uiState = uiState.copy(
            geminiNanoAvailable = com.najmi.oreamnos.vision.GeminiNanoExtractor.isAvailable(getApplication()),
            installedMediaPipeModels = modelManager.getInstalledModels()
        )
    }

    /**
     * Called when an image is selected.
     */
    fun onImageSelected(bitmap: Bitmap) {
        uiState = uiState.copy(selectedBitmap = bitmap, error = null)
        processImage(bitmap)
    }

    /**
     * Processes the image using the best available vision extractor.
     */
    private fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            uiState = uiState.copy(isExtracting = true, showFallbackNotice = false)
            
            // Create best available extractor
            val extractor = extractorFactory.create()
            activeExtractor = extractor
            uiState = uiState.copy(activeExtractorModel = extractor.model)

            val result = extractor.extractFromImage(bitmap)
            
            if (result.error != null) {
                // If vision failed, try ML Kit fallback silently
                if (extractor.model != VisionModel.ML_KIT) {
                    val fallback = extractorFactory.create(VisionModel.ML_KIT.id)
                    val fallbackResult = fallback.extractFromImage(bitmap)
                    
                    uiState = uiState.copy(
                        editableText = fallbackResult.extractedText,
                        extractionResult = fallbackResult,
                        isExtracting = false,
                        showFallbackNotice = true,
                        activeExtractorModel = VisionModel.ML_KIT
                    )
                } else {
                    uiState = uiState.copy(
                        error = result.error,
                        isExtracting = false
                    )
                }
            } else {
                uiState = uiState.copy(
                    editableText = result.extractedText,
                    extractionResult = result,
                    isExtracting = false
                )
            }
            
            extractor.release()
            activeExtractor = null
        }
    }

    /**
     * Handles model download request.
     */
    fun startDownload(model: VisionModel) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            uiState = uiState.copy(isModelDownloading = true, modelDownloadProgress = 0f)
            
            val result = modelManager.downloadModel(model, onProgress = { progress ->
                uiState = uiState.copy(modelDownloadProgress = progress)
            })
            
            uiState = uiState.copy(isModelDownloading = false)
            
            if (result.isSuccess) {
                refreshModelState()
                // Auto-retry extraction with new model if image is already selected
                uiState.selectedBitmap?.let { processImage(it) }
            } else {
                uiState = uiState.copy(error = "Download failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        uiState = uiState.copy(isModelDownloading = false)
    }

    fun dismissFallbackNotice() {
        uiState = uiState.copy(showFallbackNotice = false)
    }

    fun clearState() {
        uiState = OcrUiState(
            geminiNanoAvailable = uiState.geminiNanoAvailable,
            installedMediaPipeModels = uiState.installedMediaPipeModels
        )
        activeExtractor?.release()
        activeExtractor = null
    }

    fun retry() {
        uiState.selectedBitmap?.let { processImage(it) }
    }

    fun updateEditableText(text: String) {
        uiState = uiState.copy(editableText = text)
    }

    fun clearSelection() {
        uiState = OcrUiState(
            geminiNanoAvailable = uiState.geminiNanoAvailable,
            installedMediaPipeModels = uiState.installedMediaPipeModels
        )
        activeExtractor?.release()
        activeExtractor = null
    }

    override fun onCleared() {
        super.onCleared()
        activeExtractor?.release()
    }
}
