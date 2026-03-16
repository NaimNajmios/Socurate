package com.najmi.oreamnos.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litertlm.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LiteRTEngine(private val context: Context) {
    
    private var engine: Engine? = null
    private var currentModelPath: String? = null
    
    companion object {
        private const val TAG = "LiteRTEngine"
        val DEFAULT_PROMPT = """
            Extract all football statistics from this image. 
            Return structured labels only:
            Match: [home] vs [away]
            Score: [n-n]
            Competition: [name]
            Scorers: [player] [minute]
            Player: [name] | Goals:[n] Assists:[n] Rating:[n]
            Other: [any relevant stats]
        """.trimIndent()
    }
    
    suspend fun initialize(
        modelPath: String,
        enableVision: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (engine != null && currentModelPath == modelPath) {
                return@withContext Result.success(Unit)
            }
            
            release()
            
            val backend = try { Backend.GPU() } catch (e: Exception) { 
                Log.w(TAG, "GPU unavailable, using CPU")
                Backend.CPU() 
            }
            
            val config = EngineConfig(
                modelPath = modelPath,
                backend = backend,
                cacheDir = context.cacheDir.path
            )
            
            val newEngine = Engine(config)
            newEngine.initialize()
            
            engine = newEngine
            currentModelPath = modelPath
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Init failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun extractFromImage(
        imagePath: String,
        prompt: String = DEFAULT_PROMPT
    ): Result<String> = withContext(Dispatchers.IO) {
        if (engine == null) {
            return@withContext Result.failure(Exception("Engine not initialized"))
        }
        
        try {
            val message = Message.of(
                Content.ImageFile(imagePath),
                Content.Text(prompt)
            )
            
            val conv = engine!!.createConversation()
            val response = conv.sendMessage(message)
            conv.close()
            
            // Try different ways to get text if possible
            Result.success(response.toString())
        } catch (e: LiteRtLmJniException) {
            Result.failure(Exception("Inference error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun extractFromBitmap(
        bitmap: Bitmap,
        prompt: String = DEFAULT_PROMPT
    ): Result<String> = withContext(Dispatchers.IO) {
        val imageFile = saveBitmapToFile(bitmap)
        try {
            extractFromImage(imageFile.absolutePath, prompt)
        } finally {
            imageFile.delete()
        }
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(context.cacheDir, "litert_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file
    }
    
    fun release() {
        try {
            engine?.close()
            engine = null
            currentModelPath = null
        } catch (e: Exception) {
            Log.e(TAG, "Release error: ${e.message}")
        }
    }
}
