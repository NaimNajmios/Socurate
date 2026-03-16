package com.najmi.oreamnos.utils

import android.content.Context
import com.najmi.oreamnos.vision.VisionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

/**
 * Manages LiteRT model files (downloading, deleting, checking availability).
 * Models are downloaded from HuggingFace LiteRT Community.
 * 
 * Supported models:
 * - Gemma 3n E2B (~2.9GB) - Multimodal
 * - Gemma 3 1B (~557MB) - Text only
 */
class VisionModelManager(private val context: Context) {

    private val client = OkHttpClient()
    private val modelsDir = File(context.filesDir, "models")

    init {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
    }

    /**
     * Checks if the model file exists on disk.
     */
    fun isModelAvailable(model: VisionModel): Boolean {
        if (!model.requiresDownload) return true
        return File(getModelPath(model)).exists()
    }

    /**
     * Checks if model is available by ID string.
     */
    fun isModelAvailable(modelId: String): Boolean {
        val model = VisionModel.fromId(modelId)
        return isModelAvailable(model)
    }

    /**
     * Returns the absolute path to the model file.
     * Model files use .task extension for LiteRT bundles.
     */
    fun getModelPath(model: VisionModel): String {
        return File(modelsDir, "${model.id}.task").absolutePath
    }

    /**
     * Returns the absolute path to the model file by ID.
     */
    fun getModelPath(modelId: String): String {
        val model = VisionModel.fromId(modelId)
        return getModelPath(model)
    }

    /**
     * Downloads a model file from HuggingFace with progress updates.
     * Uses streaming download to handle large files efficiently.
     * 
     * @param model The model to download
     * @param onProgress Callback for progress updates (0.0 to 1.0)
     * @param hfToken Optional HuggingFace token for gated models
     */
    suspend fun downloadModel(
        model: VisionModel,
        onProgress: (Float) -> Unit,
        hfToken: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val url = model.downloadUrl 
            ?: return@withContext Result.failure(Exception("No download URL for ${model.displayName}"))
        
        val targetFile = File(getModelPath(model))
        
        // Delete partial download if exists
        if (targetFile.exists()) {
            targetFile.delete()
        }
        
        try {
            val requestBuilder = Request.Builder().url(url)
            
            // Add HuggingFace token header if provided
            if (!hfToken.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $hfToken")
            }
            
            val request = requestBuilder.build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to download model: ${response.code} ${response.message}")
                }
                
                val body = response.body 
                    ?: throw Exception("Response body is null")
                
                val contentLength = body.contentLength()
                val isChunked = contentLength == -1L
                
                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            if (!isChunked && contentLength > 0) {
                                val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                onProgress(progress.coerceIn(0f, 1f))
                            } else if (isChunked) {
                                // For chunked responses, show indeterminate progress
                                onProgress(-1f)
                            }
                        }
                    }
                }
            }
            
            // Verify file exists and has content
            if (!targetFile.exists() || targetFile.length() == 0L) {
                throw Exception("Downloaded file is invalid")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Clean up failed download
            if (targetFile.exists()) {
                targetFile.delete()
            }
            Result.failure(e)
        }
    }

    /**
     * Downloads model as a Flow for reactive progress updates.
     */
    fun downloadModelFlow(
        model: VisionModel,
        hfToken: String? = null
    ): Flow<DownloadProgress> = flow {
        val url = model.downloadUrl 
            ?: throw Exception("No download URL for ${model.displayName}")
        
        val targetFile = File(getModelPath(model))
        
        // Delete partial download if exists
        if (targetFile.exists()) {
            targetFile.delete()
        }
        
        emit(DownloadProgress.Started(model))
        
        try {
            val requestBuilder = Request.Builder().url(url)
            
            if (!hfToken.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $hfToken")
            }
            
            val request = requestBuilder.build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to download: ${response.code}")
                }
                
                val body = response.body 
                    ?: throw Exception("Response body is null")
                
                val contentLength = body.contentLength()
                
                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            if (contentLength > 0) {
                                val progress = (totalBytesRead.toFloat() / contentLength.toFloat())
                                val downloadedMb = totalBytesRead / (1024 * 1024)
                                val totalMb = contentLength / (1024 * 1024)
                                emit(DownloadProgress.InProgress(
                                    progress = progress,
                                    downloadedMb = downloadedMb,
                                    totalMb = totalMb
                                ))
                            }
                        }
                    }
                }
            }
            
            emit(DownloadProgress.Completed(targetFile.absolutePath))
            
        } catch (e: Exception) {
            if (targetFile.exists()) {
                targetFile.delete()
            }
            emit(DownloadProgress.Failed(e.message ?: "Download failed"))
        }
    }

    /**
     * Deletes a model file from disk.
     */
    fun deleteModel(model: VisionModel) {
        val file = File(getModelPath(model))
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Deletes a model by ID string.
     */
    fun deleteModel(modelId: String) {
        val model = VisionModel.fromId(modelId)
        deleteModel(model)
    }

    /**
     * Returns a list of all downloaded models.
     */
    fun getInstalledModels(): List<VisionModel> {
        return VisionModel.entries.filter { it.requiresDownload && isModelAvailable(it) }
    }

    /**
     * Returns the storage used by a specific model in MB.
     */
    fun getStorageUsedMb(model: VisionModel): Long {
        val file = File(getModelPath(model))
        return if (file.exists()) file.length() / (1024 * 1024) else 0L
    }

    /**
     * Returns total storage used by all downloaded models in MB.
     */
    fun getTotalStorageUsedMb(): Long {
        return getInstalledModels().sumOf { getStorageUsedMb(it) }
    }

    /**
     * Gets model by ID string, returning null if not available.
     */
    fun getModel(modelId: String): VisionModel? {
        val model = VisionModel.fromId(modelId)
        return if (isModelAvailable(model)) model else null
    }

    /**
     * Returns available space on device in MB.
     */
    fun getAvailableSpaceMb(): Long {
        return modelsDir.freeSpace / (1024 * 1024)
    }

    sealed class DownloadProgress {
        data class Started(val model: VisionModel) : DownloadProgress()
        data class InProgress(
            val progress: Float,
            val downloadedMb: Long,
            val totalMb: Long
        ) : DownloadProgress()
        data class Completed(val filePath: String) : DownloadProgress()
        data class Failed(val error: String) : DownloadProgress()
    }
}
