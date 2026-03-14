package com.najmi.oreamnos.model

import com.najmi.oreamnos.vision.VisionModel

/**
 * Result of a vision extraction operation.
 */
data class VisionExtractionResult(
    val extractedText: String,
    val source: VisionModel,
    val durationMs: Long,
    val error: String? = null
)
