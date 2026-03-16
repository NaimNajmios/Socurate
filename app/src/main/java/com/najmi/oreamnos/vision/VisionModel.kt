package com.najmi.oreamnos.vision

import kotlin.collections.*

/**
 * Defines all supported extraction paths as an enum with metadata.
 * 
 * Models are downloaded from HuggingFace LiteRT Community:
 * - Gemma 3n: https://huggingface.co/litert-community
 * - Gemma 3: https://huggingface.co/litert-community
 * 
 * Note: Full LiteRT integration requires Kotlin 2.0+. Currently using ML Kit OCR.
 */
enum class VisionModel(
    val id: String,
    val displayName: String,
    val description: String,
    val requiresDownload: Boolean,
    val approximateSizeMb: Int,
    val minimumRamGb: Int,
    val downloadUrl: String? = null,
    val isMultimodal: Boolean = false
) {
    GEMINI_NANO(
        id = "gemini_nano",
        displayName = "On-Device OCR",
        description = "ML Kit text recognition with intelligent structuring.",
        requiresDownload = false,
        approximateSizeMb = 0,
        minimumRamGb = 4,
        isMultimodal = false
    ),
    GEMMA_3N_E2B(
        id = "gemma_3n_e2b",
        displayName = "Gemma 3n E2B",
        description = "Multimodal model with vision + text. ~2.9GB. Best for screenshots.",
        requiresDownload = true,
        approximateSizeMb = 2965,
        minimumRamGb = 6,
        downloadUrl = "https://huggingface.co/litert-community/Gemma-3n-E2B-it-int4/resolve/main/gemma-3n-e2b-it-int4.task",
        isMultimodal = true
    ),
    GEMMA_3_1B(
        id = "gemma_3_1b",
        displayName = "Gemma 3 1B",
        description = "Text-only model. ~557MB. Fast, works with OCR text.",
        requiresDownload = true,
        approximateSizeMb = 557,
        minimumRamGb = 4,
        downloadUrl = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task",
        isMultimodal = false
    ),
    PALIGEMMA_3B(
        id = "paligemma_3b",
        displayName = "PaliGemma 3B",
        description = "Vision language model. ~3GB. Legacy option.",
        requiresDownload = true,
        approximateSizeMb = 3000,
        minimumRamGb = 8,
        downloadUrl = null, 
        isMultimodal = true
    ),
    ML_KIT(
        id = "ml_kit",
        displayName = "OCR Fallback",
        description = "Basic text extraction. No AI interpretation.",
        requiresDownload = false,
        approximateSizeMb = 0,
        minimumRamGb = 2,
        isMultimodal = false
    );

    companion object {
        fun fromId(id: String): VisionModel {
            val v = VisionModel.values()
            for (i in 0 until v.size) {
                if (v[i].id == id) return v[i]
            }
            return ML_KIT
        }
        
        fun getDownloadableModels(): List<VisionModel> {
            val list = java.util.ArrayList<VisionModel>()
            val v = VisionModel.values()
            for (i in 0 until v.size) {
                val m = v[i]
                if (m.requiresDownload && m.downloadUrl != null) {
                    list.add(m)
                }
            }
            return list
        }
        
        fun getMultimodalModels(): List<VisionModel> {
            val list = java.util.ArrayList<VisionModel>()
            val v = VisionModel.values()
            for (i in 0 until v.size) {
                val m = v[i]
                if (m.isMultimodal) {
                    list.add(m)
                }
            }
            return list
        }
    }
}
