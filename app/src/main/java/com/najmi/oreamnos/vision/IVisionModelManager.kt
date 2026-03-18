package com.najmi.oreamnos.vision

import kotlinx.coroutines.flow.Flow

sealed class VisionDownloadProgress {
    data class Started(val model: VisionModel) : VisionDownloadProgress()
    data class InProgress(
        val progress: Float,
        val downloadedMb: Long,
        val totalMb: Long
    ) : VisionDownloadProgress()
    data class Completed(val filePath: String) : VisionDownloadProgress()
    data class Failed(val error: String) : VisionDownloadProgress()
}

interface IVisionModelManager {
    fun isModelAvailable(model: VisionModel): Boolean
    fun isModelAvailable(modelId: String): Boolean
    fun getModelPath(model: VisionModel): String
    fun getModelPath(modelId: String): String
    suspend fun downloadModel(
        model: VisionModel,
        onProgress: (Float) -> Unit,
        hfToken: String? = null
    ): Result<Unit>
    fun downloadModelFlow(model: VisionModel, hfToken: String? = null): Flow<VisionDownloadProgress>
    fun deleteModel(model: VisionModel)
    fun deleteModel(modelId: String)
    fun getInstalledModels(): List<VisionModel>
    fun getStorageUsedMb(model: VisionModel): Long
    fun getTotalStorageUsedMb(): Long
    fun getModel(modelId: String): VisionModel?
    fun getAvailableSpaceMb(): Long
}
