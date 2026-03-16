package com.najmi.oreamnos.vision

import android.content.Context
import com.najmi.oreamnos.utils.VisionModelManager

/**
 * Factory to create the best available [IVisionExtractor].
 * 
 * Priority order for auto-selection:
 * 1. Gemma 3n E2B (multimodal) - requires Kotlin 2.0+ for LiteRT
 * 2. Gemma 3 1B (text-only) - requires Kotlin 2.0+ for LiteRT
 * 3. ML Kit OCR (fallback)
 */
class VisionExtractorFactory(
    private val context: Context,
    private val modelManager: VisionModelManager
) {

    /**
     * Creates an extractor based on user preference or automatic best-available logic.
     *
     * @param preferredModelId The model ID the user has manually selected (if any).
     */
    fun create(preferredModelId: String? = null): IVisionExtractor {
        // 1. If user has a pinned preference, try that first
        preferredModelId?.let { id ->
            val preferredModel = VisionModel.fromId(id)
            if (isModelAvailable(preferredModel)) {
                return createExtractor(preferredModel)
            }
        }

        // 2. Auto-selection - for now, use ML Kit OCR
        // Full LiteRT integration when Kotlin 2.0+ is available
        return createExtractor(VisionModel.ML_KIT)
    }

    /**
     * Checks if a specific model is available.
     */
    fun isModelAvailable(model: VisionModel): Boolean {
        return when (model) {
            VisionModel.GEMINI_NANO, VisionModel.ML_KIT -> true
            else -> modelManager.isModelAvailable(model)
        }
    }

    /**
     * Gets the best available model for auto mode.
     */
    fun getBestAvailableModel(): VisionModel {
        // Currently only ML Kit is available without LiteRT
        return VisionModel.ML_KIT
    }

    /**
     * Gets all available models for UI display.
     */
    fun getAvailableModels(): List<VisionModel> {
        return listOf(VisionModel.ML_KIT)
    }

    private fun createExtractor(model: VisionModel): IVisionExtractor {
        return when (model) {
            // ML Kit OCR based
            VisionModel.GEMINI_NANO -> GeminiNanoExtractor(context)
            VisionModel.ML_KIT -> MlKitFallbackExtractor()
            
            // LiteRT models - show as available but use fallback
            // Full implementation when Kotlin 2.0+ is available
            else -> MlKitFallbackExtractor()
        }
    }
}
