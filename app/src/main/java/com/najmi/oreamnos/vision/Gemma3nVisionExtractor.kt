package com.najmi.oreamnos.vision

import android.content.Context
import android.graphics.Bitmap
import com.najmi.oreamnos.model.VisionExtractionResult
import com.najmi.oreamnos.utils.VisionModelManager

class Gemma3nVisionExtractor(
    private val context: Context,
    private val modelManager: VisionModelManager
) : IVisionExtractor {
    
    override val model: VisionModel = VisionModel.GEMMA_3N_E2B
    private val engine = LiteRTEngine(context)

    override suspend fun extractFromImage(bitmap: Bitmap): VisionExtractionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            val modelPath = modelManager.getModelPath(model)
            engine.initialize(modelPath, enableVision = true).getOrThrow()
            
            val result = engine.extractFromBitmap(bitmap)
            val duration = System.currentTimeMillis() - startTime
            
            result.fold(
                onSuccess = { text ->
                    VisionExtractionResult(text, model, duration)
                },
                onFailure = { e ->
                    VisionExtractionResult("", model, duration, e.message)
                }
            )
        } catch (e: Exception) {
            VisionExtractionResult("", model, 
                System.currentTimeMillis() - startTime, e.message)
        }
    }

    override fun release() = engine.release()
}
