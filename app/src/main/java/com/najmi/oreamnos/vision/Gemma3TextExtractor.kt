package com.najmi.oreamnos.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.najmi.oreamnos.model.VisionExtractionResult
import com.najmi.oreamnos.utils.VisionModelManager
import kotlinx.coroutines.tasks.await

class Gemma3TextExtractor(
    private val context: Context,
    private val modelManager: VisionModelManager
) : IVisionExtractor {
    
    override val model: VisionModel = VisionModel.GEMMA_3_1B
    private val ocr = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val engine = LiteRTEngine(context)

    override suspend fun extractFromImage(bitmap: Bitmap): VisionExtractionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Step 1: OCR
            val ocrResult = ocr.process(InputImage.fromBitmap(bitmap, 0)).await()
            if (ocrResult.text.isBlank()) {
                return VisionExtractionResult("", model, 0, "No text detected")
            }
            
            // Step 2: Initialize model
            val modelPath = modelManager.getModelPath(model)
            engine.initialize(modelPath, enableVision = false).getOrThrow()
            
            // Step 3: Structure text
            val result = engine.extractFromBitmap(bitmap, "Structure this football data: ${ocrResult.text}")
            val duration = System.currentTimeMillis() - startTime
            
            result.fold(
                onSuccess = { text ->
                    VisionExtractionResult(text, model, duration)
                },
                onFailure = {
                    // Fallback to raw OCR
                    VisionExtractionResult(ocrResult.text, model, duration, "Using raw OCR")
                }
            )
        } catch (e: Exception) {
            VisionExtractionResult("", model, 
                System.currentTimeMillis() - startTime, e.message)
        }
    }

    override fun release() {
        ocr.close()
        engine.release()
    }
}
