package com.najmi.oreamnos.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.najmi.oreamnos.model.VisionExtractionResult
import com.najmi.oreamnos.utils.VisionModelManager
import kotlinx.coroutines.tasks.await

/**
 * Extractor using ML Kit OCR.
 * Note: True PaliGemma via MediaPipe/LiteRT requires model download and specific runtime setup.
 * This uses ML Kit OCR for text extraction.
 */
class PaliGemmaExtractor(
    private val context: Context,
    private val modelManager: VisionModelManager
) : IVisionExtractor {
    override val model: VisionModel = VisionModel.PALIGEMMA_2_3B

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractFromImage(bitmap: Bitmap): VisionExtractionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage).await()
            val rawText = result.text
            
            if (rawText.isEmpty()) {
                throw Exception("No text detected in image")
            }
            
            val duration = System.currentTimeMillis() - startTime
            VisionExtractionResult(
                extractedText = rawText,
                source = model,
                durationMs = duration
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            VisionExtractionResult(
                extractedText = "",
                source = model,
                durationMs = duration,
                error = e.message ?: "Extraction failed"
            )
        }
    }

    override fun release() {
        try {
            textRecognizer.close()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
