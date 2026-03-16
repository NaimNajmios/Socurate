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
            val model = VisionModel.fromId(id)
            if (isModelAvailable(model)) return createExtractor(model)
        }

        // 2. Auto-selection priority
        return when {
            modelManager.isModelAvailable(VisionModel.GEMMA_3N_E2B) -> 
                createExtractor(VisionModel.GEMMA_3N_E2B)
            modelManager.isModelAvailable(VisionModel.GEMMA_3_1B) -> 
                createExtractor(VisionModel.GEMMA_3_1B)
            else -> createExtractor(VisionModel.ML_KIT)
        }
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
        return when {
            modelManager.isModelAvailable(VisionModel.GEMMA_3N_E2B) -> VisionModel.GEMMA_3N_E2B
            modelManager.isModelAvailable(VisionModel.GEMMA_3_1B) -> VisionModel.GEMMA_3_1B
            else -> VisionModel.ML_KIT
        }
    }

    /**
     * Gets all available models for UI display.
     */
    fun getAvailableModels(): List<VisionModel> {
        val models = mutableListOf<VisionModel>()
        models.add(VisionModel.ML_KIT)
        if (modelManager.isModelAvailable(VisionModel.GEMMA_3N_E2B)) models.add(VisionModel.GEMMA_3N_E2B)
        if (modelManager.isModelAvailable(VisionModel.GEMMA_3_1B)) models.add(VisionModel.GEMMA_3_1B)
        return models
    }

    private fun createExtractor(model: VisionModel): IVisionExtractor {
        return when (model) {
            VisionModel.GEMMA_3N_E2B -> Gemma3nVisionExtractor(context, modelManager)
            VisionModel.GEMMA_3_1B -> Gemma3TextExtractor(context, modelManager)
            VisionModel.PALIGEMMA_3B -> Gemma3TextExtractor(context, modelManager) // Fallback to same logic if needed
            VisionModel.GEMINI_NANO -> GeminiNanoExtractor(context)
            else -> MlKitFallbackExtractor()
        }
    }
}
