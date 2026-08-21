package com.mehmetbozkurt.questlog.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProofPhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir: File
        get() = File(context.filesDir, "proofs").apply { mkdirs() }

    fun newCaptureTarget(logId: String): Pair<File, Uri> {
        val file = File(dir, "capture_$logId.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return file to uri
    }

    suspend fun importFromUri(logId: String, source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = context.contentResolver.openInputStream(source).use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return@runCatching null

            val rotated = context.contentResolver.openInputStream(source).use { input ->
                if (input == null) bitmap else applyExifRotation(bitmap, ExifInterface(input))
            }

            writeCompressed(logId, rotated)
        }.getOrNull()
    }

    suspend fun importFromFile(logId: String, file: File): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@runCatching null
            val rotated = applyExifRotation(bitmap, ExifInterface(file.absolutePath))
            val path = writeCompressed(logId, rotated)
            file.delete()
            path
        }.getOrNull()
    }

    fun delete(path: String?) {
        if (path == null) return
        runCatching { File(path).delete() }
    }

    fun exists(path: String?): Boolean = path != null && File(path).exists()

    private fun writeCompressed(logId: String, bitmap: Bitmap): String {
        val scaled = downscale(bitmap)
        val target = File(dir, "$logId.jpg")
        FileOutputStream(target).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        }
        if (scaled != bitmap) scaled.recycle()
        bitmap.recycle()
        return target.absolutePath
    }

    private fun downscale(bitmap: Bitmap): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= MAX_EDGE) return bitmap
        val ratio = MAX_EDGE.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true,
        )
    }

    private fun applyExifRotation(bitmap: Bitmap, exif: ExifInterface): Bitmap {
        val degrees = when (
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true,
        )
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    private companion object {
        const val MAX_EDGE = 1080
        const val QUALITY = 80
    }
}
