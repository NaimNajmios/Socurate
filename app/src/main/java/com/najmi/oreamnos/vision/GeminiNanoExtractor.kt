package com.najmi.oreamnos.vision

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.najmi.oreamnos.model.VisionExtractionResult
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Extractor using ML Kit's built-in text recognition.
 * This provides on-device OCR for text extraction from screenshots.
 */
class GeminiNanoExtractor(private val context: Context) : IVisionExtractor {
    override val model: VisionModel = VisionModel.GEMINI_NANO

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
                error = e.message ?: "Text recognition failed"
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

    companion object {
        fun isAvailable(context: Context): Boolean {
            return true
        }
    }
}
