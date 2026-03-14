package com.najmi.oreamnos.vision

import android.graphics.Bitmap
import com.najmi.oreamnos.model.VisionExtractionResult

/**
 * Common interface for all vision extraction paths.
 */
interface IVisionExtractor {
    /**
     * The model type used by this extractor.
     */
    val model: VisionModel

    /**
     * Extracts structured football data from the given bitmap.
     *
     * @param bitmap The image to process.
     * @return Result containing extracted text and metadata.
     */
    suspend fun extractFromImage(bitmap: Bitmap): VisionExtractionResult

    /**
     * Releases any resources held by the extractor.
     */
    fun release()
}
