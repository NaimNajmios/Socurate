package com.najmi.oreamnos.vision

import android.graphics.Bitmap
import com.najmi.oreamnos.model.VisionExtractionResult
import com.najmi.oreamnos.utils.FootballOcrParser
import com.najmi.oreamnos.utils.OcrUtils

/**
 * Fallback extractor using ML Kit OCR.
 * Wraps existing [OcrUtils] and [FootballOcrParser].
 */
class MlKitFallbackExtractor : IVisionExtractor {
    override val model: VisionModel = VisionModel.ML_KIT

    override suspend fun extractFromImage(bitmap: Bitmap): VisionExtractionResult {
        val startTime = System.currentTimeMillis()
        return try {
            val ocrResult = OcrUtils.extractTextFromBitmap(bitmap)
            val rawText = ocrResult.getOrThrow()
            val structuredText = FootballOcrParser.formatForPrompt(rawText)
            val duration = System.currentTimeMillis() - startTime
            
            VisionExtractionResult(
                extractedText = structuredText,
                source = model,
                durationMs = duration
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            VisionExtractionResult(
                extractedText = "",
                source = model,
                durationMs = duration,
                error = e.message ?: "OCR Fallback failed"
            )
        }
    }

    override fun release() {
        // No-op for ML Kit fallback
    }
}
