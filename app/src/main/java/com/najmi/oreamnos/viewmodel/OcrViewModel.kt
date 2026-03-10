package com.najmi.oreamnos.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najmi.oreamnos.utils.FootballOcrParser
import com.najmi.oreamnos.utils.OcrUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for the OCR flow.
 * Manages image selection, processing state, and extracted results.
 */
class OcrViewModel : ViewModel() {

    // UI State
    var isLoading by mutableStateOf(false)
        private set

    var selectedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var extractedText by mutableStateOf("")
    
    var error by mutableStateOf<String?>(null)
        private set

    /**
     * Called when an image is selected from gallery or captured via camera.
     */
    fun onImageSelected(bitmap: Bitmap) {
        selectedBitmap = bitmap
        processImage(bitmap)
    }

    /**
     * Extracts text from the bitmap and applies football-specific parsing.
     */
    private fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            isLoading = true
            error = null
            
            val result = OcrUtils.extractTextFromBitmap(bitmap)
            
            result.onSuccess { rawText ->
                extractedText = FootballOcrParser.formatForPrompt(rawText)
                isLoading = false
            }.onFailure { e ->
                error = e.message ?: "Failed to extract text from image"
                isLoading = false
            }
        }
    }

    /**
     * Clears all state.
     */
    fun clearState() {
        selectedBitmap = null
        extractedText = ""
        isLoading = false
        error = null
    }

    /**
     * Retries processing for the currently selected bitmap.
     */
    fun retry() {
        selectedBitmap?.let { processImage(it) }
    }
}
