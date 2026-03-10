package com.najmi.oreamnos.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Utility for extracting text from images using ML Kit.
 */
object OcrUtils {

    /**
     * Extracts text from the provided [Bitmap].
     * Runs on Dispatchers.IO.
     * 
     * @param bitmap The image to process.
     * @return Result containing the extracted text or an exception.
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    recognizer.close()
                    if (continuation.isActive) {
                        continuation.resume(Result.success(text))
                    }
                }
                .addOnFailureListener { e ->
                    recognizer.close()
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
        }
    }
}
