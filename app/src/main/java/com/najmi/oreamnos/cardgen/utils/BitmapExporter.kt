package com.najmi.oreamnos.cardgen.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saves and shares exported card bitmaps.
 *
 * - [saveToGallery]: writes [Bitmap] to the public Pictures gallery via [MediaStore]
 *   (no WRITE_EXTERNAL_STORAGE permission needed on API 29+).
 * - [shareImage]: writes to the app's cache directory and fires an ACTION_SEND
 *   intent via [FileProvider] so the receiving app can access the file.
 */
object BitmapExporter {

    private const val TAG = "BitmapExporter"
    private const val ALBUM_NAME = "Socurate"

    /**
     * Saves [bitmap] to the device gallery (Pictures/Socurate/).
     *
     * @return [Uri] of the saved image, for use in share intents.
     */
    suspend fun saveToGallery(bitmap: Bitmap, context: Context): Uri = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "socurate_card_$timestamp.jpg"

        val savedUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ — use MediaStore (no WRITE_EXTERNAL_STORAGE needed)
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                contentValues
            ) ?: throw Exception("MediaStore insert returned null")

            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            uri
        } else {
            // API 24-28 — write to file, then scan
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME"
            )
            picturesDir.mkdirs()
            val file = File(picturesDir, filename)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            // Notify MediaStore
            context.sendBroadcast(
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))
            )
            Uri.fromFile(file)
        }

        Log.d(TAG, "Saved card to gallery: $savedUri")
        savedUri
    }

    /**
     * Saves [bitmap] to the app's cache directory and fires an ACTION_SEND share intent.
     *
     * Uses [FileProvider] so the receiving app (Instagram, WhatsApp, etc.) can read the file.
     */
    suspend fun shareImage(bitmap: Bitmap, context: Context) = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "card_exports").also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(cacheDir, "socurate_card_$timestamp.jpg")

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }

        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Log.d(TAG, "Sharing card via FileProvider: $shareUri")

        withContext(Dispatchers.Main) {
            context.startActivity(
                Intent.createChooser(shareIntent, "Kongsi Kad Socurate")
                    .also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }
    }
}
