package com.najmi.oreamnos.vision

/**
 * Defines all supported extraction paths as an enum with metadata.
 * 
 * Note: True on-device vision models (PaliGemma, Gemma 3) require model files
 * that need to be downloaded separately. The URLs below are placeholders.
 */
enum class VisionModel(
    val id: String,
    val displayName: String,
    val description: String,
    val requiresDownload: Boolean,
    val approximateSizeMb: Int,
    val minimumRamGb: Int,
    val downloadUrl: String? = null
) {
    GEMINI_NANO(
        id = "gemini_nano",
        displayName = "On-Device OCR",
        description = "ML Kit text recognition with intelligent structuring.",
        requiresDownload = false,
        approximateSizeMb = 0,
        minimumRamGb = 4
    ),
    PALIGEMMA_2_3B(
        id = "paligemma_2_3b",
        displayName = "PaliGemma 2 3B",
        description = "Optimized on-device vision model. Balanced performance.",
        requiresDownload = true,
        approximateSizeMb = 1500,
        minimumRamGb = 6,
        downloadUrl = null // Placeholder - requires model file
    ),
    GEMMA_3_4B(
        id = "gemma_3_4b",
        displayName = "Gemma 3 4B",
        description = "High-performance on-device vision model. Flagship devices.",
        requiresDownload = true,
        approximateSizeMb = 2000,
        minimumRamGb = 8,
        downloadUrl = null // Placeholder - requires model file
    ),
    ML_KIT(
        id = "ml_kit",
        displayName = "OCR Fallback",
        description = "Basic text extraction. No AI interpretation.",
        requiresDownload = false,
        approximateSizeMb = 0,
        minimumRamGb = 2
    );

    companion object {
        fun fromId(id: String): VisionModel = entries.find { it.id == id } ?: ML_KIT
    }
}
