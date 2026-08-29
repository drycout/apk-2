package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object AppStorageHelper {

    const val FOLDER_ROOT = "DP"
    const val SUBFOLDER_CSV = "CSV"
    const val SUBFOLDER_PDF = "PDF"
    const val SUBFOLDER_JSON = "JSON"
    const val SUBFOLDER_NOTA = "NOTA"

    /**
     * Saves text or binary data (CSV, PDF, JSON) into public Downloads/DP/<subFolder>/
     * Automatically creates the folder structure on internal storage.
     */
    suspend fun saveToDownloads(
        context: Context,
        subFolder: String,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val relativePath = "Download/$FOLDER_ROOT/$subFolder"
            val displayPath = "Download/$FOLDER_ROOT/$subFolder/$fileName"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Gagal membuat berkas MediaStore")

                resolver.openOutputStream(uri)?.use { os ->
                    writeContent(os)
                } ?: throw Exception("Gagal membuka aliran berkas")

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success("Tersimpan di $displayPath")
            } else {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadDir, "$FOLDER_ROOT/$subFolder")
                if (!targetDir.exists()) targetDir.mkdirs()

                val file = File(targetDir, fileName)
                FileOutputStream(file).use { os ->
                    writeContent(os)
                }
                Result.success("Tersimpan di $displayPath")
            }
        } catch (e: Exception) {
            try {
                // Fallback to external files dir
                val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val targetDir = File(baseDir, "$FOLDER_ROOT/$subFolder")
                if (!targetDir.exists()) targetDir.mkdirs()
                val file = File(targetDir, fileName)
                FileOutputStream(file).use { os ->
                    writeContent(os)
                }
                Result.success("Tersimpan di folder $FOLDER_ROOT/$subFolder/$fileName")
            } catch (fallbackErr: Exception) {
                Result.failure(fallbackErr)
            }
        }
    }

    /**
     * Saves Receipt Bitmap into public Pictures/DP/NOTA/ or Downloads/DP/NOTA/
     */
    suspend fun saveReceiptImageToStorage(
        context: Context,
        fileName: String,
        bitmap: Bitmap
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val relativePath = "Pictures/$FOLDER_ROOT/$SUBFOLDER_NOTA"
            val displayPath = "Pictures/$FOLDER_ROOT/$SUBFOLDER_NOTA/$fileName"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Gagal membuat berkas gambar")

                resolver.openOutputStream(uri)?.use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                } ?: throw Exception("Gagal menyimpan gambar")

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success("Nota tersimpan di $displayPath")
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val targetDir = File(picturesDir, "$FOLDER_ROOT/$SUBFOLDER_NOTA")
                if (!targetDir.exists()) targetDir.mkdirs()

                val file = File(targetDir, fileName)
                FileOutputStream(file).use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }

                val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                scanIntent.data = Uri.fromFile(file)
                context.sendBroadcast(scanIntent)

                Result.success("Nota tersimpan di $displayPath")
            }
        } catch (e: Exception) {
            try {
                val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
                val targetDir = File(baseDir, "$FOLDER_ROOT/$SUBFOLDER_NOTA")
                if (!targetDir.exists()) targetDir.mkdirs()
                val file = File(targetDir, fileName)
                FileOutputStream(file).use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
                Result.success("Nota tersimpan di folder $FOLDER_ROOT/$SUBFOLDER_NOTA/$fileName")
            } catch (fallbackErr: Exception) {
                Result.failure(fallbackErr)
            }
        }
    }

    /**
     * Copies an image picked from Gallery/Storage (content:// URI) into the app's persistent internal storage
     * Returns the absolute path of the saved file or file URI.
     */
    suspend fun saveImageFromUri(
        context: Context,
        sourceUri: Uri,
        prefix: String = "img"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.filesDir, "app_images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val extension = try {
                context.contentResolver.getType(sourceUri)?.let { mime ->
                    when {
                        mime.contains("png") -> "png"
                        mime.contains("webp") -> "webp"
                        mime.contains("gif") -> "gif"
                        else -> "jpg"
                    }
                } ?: "jpg"
            } catch (e: Exception) {
                "jpg"
            }

            val fileName = "${prefix}_${System.currentTimeMillis()}.$extension"
            val destFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Gagal membaca berkas gambar")

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Shares a temporary cache file via Android Share Intent
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Shares plain text / JSON text
     */
    fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
