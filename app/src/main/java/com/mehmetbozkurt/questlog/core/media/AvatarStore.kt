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
class AvatarStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir: File
        get() = File(context.filesDir, "avatars").apply { mkdirs() }

    fun newCaptureTarget(): Pair<File, Uri> {
        val file = File(dir, "capture.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return file to uri
    }

    suspend fun importFromUri(source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = context.contentResolver.openInputStream(source).use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return@runCatching null

            val rotated = context.contentResolver.openInputStream(source).use { input ->
                if (input == null) bitmap else applyExifRotation(bitmap, ExifInterface(input))
            }

            write(rotated)
        }.getOrNull()
    }

    suspend fun importFromFile(file: File): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@runCatching null
            val rotated = applyExifRotation(bitmap, ExifInterface(file.absolutePath))
            val path = write(rotated)
            file.delete()
            path
        }.getOrNull()
    }

    fun delete() {
        runCatching { File(dir, FILE_NAME).delete() }
    }

    private fun write(bitmap: Bitmap): String {
        val square = cropToSquare(bitmap)
        val scaled = downscale(square)
        val target = File(dir, FILE_NAME)
        FileOutputStream(target).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        }
        if (scaled != square) scaled.recycle()
        if (square != bitmap) square.recycle()
        bitmap.recycle()
        return target.absolutePath
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val edge = minOf(bitmap.width, bitmap.height)
        if (bitmap.width == bitmap.height) return bitmap
        return Bitmap.createBitmap(
            bitmap,
            (bitmap.width - edge) / 2,
            (bitmap.height - edge) / 2,
            edge,
            edge,
        )
    }

    private fun downscale(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= EDGE) return bitmap
        return Bitmap.createScaledBitmap(bitmap, EDGE, EDGE, true)
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
        const val FILE_NAME = "avatar.jpg"
        const val EDGE = 512
        const val QUALITY = 85
    }
}
