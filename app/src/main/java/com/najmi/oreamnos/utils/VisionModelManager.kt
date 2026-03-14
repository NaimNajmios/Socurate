package com.najmi.oreamnos.utils

import android.content.Context
import com.najmi.oreamnos.vision.VisionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Manages MediaPipe model files (downloading, deleting, checking availability).
 * Gemini Nano is excluded as it's managed by AICore.
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
     * Returns the absolute path to the model file.
     */
    fun getModelPath(model: VisionModel): String {
        return File(modelsDir, "${model.id}.tflite").absolutePath
    }

    /**
     * Downloads a model file with progress updates.
     */
    suspend fun downloadModel(
        model: VisionModel,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val url = model.downloadUrl ?: return@withContext Result.failure(Exception("No download URL for ${model.displayName}"))
        val targetFile = File(getModelPath(model))
        
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Failed to download model: ${response.code}")
                
                val body = response.body ?: throw Exception("Response body is null")
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
                                onProgress(totalBytesRead.toFloat() / contentLength.toFloat())
                            }
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (targetFile.exists()) targetFile.delete()
            Result.failure(e)
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
     * Returns a list of all downloaded MediaPipe models.
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
}
