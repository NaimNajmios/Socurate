package com.najmi.oreamnos.vision

import android.content.Context
import com.najmi.oreamnos.utils.VisionModelManager

/**
 * Factory to create the best available [IVisionExtractor].
 */
class VisionExtractorFactory(
    private val context: Context,
    private val modelManager: VisionModelManager
) {

    /**
     * Creates an extractor based on user preference or automatic best-available logic.
     *
     * @param preferredModel The model the user has manually selected (if any).
     */
    fun create(preferredModel: VisionModel? = null): IVisionExtractor {
        // 1. If user has a pinned preference, try that first
        preferredModel?.let {
            if (isModelAvailable(it)) {
                return createExtractor(it)
            }
        }

        // 2. Auto-selection priority
        return when {
            GeminiNanoExtractor.isAvailable(context) -> createExtractor(VisionModel.GEMINI_NANO)
            modelManager.isModelAvailable(VisionModel.PALIGEMMA_2_3B) -> createExtractor(VisionModel.PALIGEMMA_2_3B)
            modelManager.isModelAvailable(VisionModel.GEMMA_3_4B) -> createExtractor(VisionModel.GEMMA_3_4B)
            else -> createExtractor(VisionModel.ML_KIT)
        }
    }

    private fun isModelAvailable(model: VisionModel): Boolean {
        return when (model) {
            VisionModel.GEMINI_NANO -> GeminiNanoExtractor.isAvailable(context)
            VisionModel.ML_KIT -> true
            else -> modelManager.isModelAvailable(model)
        }
    }

    private fun createExtractor(model: VisionModel): IVisionExtractor {
        return when (model) {
            VisionModel.GEMINI_NANO -> GeminiNanoExtractor(context)
            VisionModel.PALIGEMMA_2_3B -> PaliGemmaExtractor(context, modelManager)
            VisionModel.GEMMA_3_4B -> Gemma3VisionExtractor(context, modelManager)
            VisionModel.ML_KIT -> MlKitFallbackExtractor()
        }
    }
}
