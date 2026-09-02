package com.example.m16_new_permissions.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Хранит фотографии меток в личной папке приложения.
 *
 * Снимок камеры и файл из галереи сначала попадают во временную папку в кэше: пока пользователь
 * не сохранил метку, постоянное место фото не занимает. При сохранении метки файл переносится
 * в filesDir — путь к нему остаётся рабочим и после перезапуска приложения, в отличие от
 * content-ссылки на галерею, разрешение на которую живёт только до перезагрузки.
 */
class AttractionPhotoStorage(context: Context) {

    private val appContext = context.applicationContext

    private val photosDir: File
        get() = File(appContext.filesDir, PHOTOS_DIR_NAME).apply { mkdirs() }

    private val tempDir: File
        get() = File(appContext.cacheDir, TEMP_DIR_NAME).apply { mkdirs() }

    /**
     * Готовит файл для снимка камеры и content-ссылку на него: напрямую file:// отдавать
     * стороннему приложению нельзя, поэтому путь заворачивается в FileProvider.
     */
    fun createCameraOutput(): CameraOutput {
        val file = File(tempDir, "camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
        return CameraOutput(file.absolutePath, uri)
    }

    // Копируем выбранное в галерее изображение к себе: чужая content-ссылка ненадёжна
    fun copyToTemp(source: Uri): String? {
        val file = File(tempDir, "picked_${UUID.randomUUID()}.jpg")
        return try {
            val copied = appContext.contentResolver.openInputStream(source)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false
            if (copied) file.absolutePath else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy picked photo", e)
            file.delete()
            null
        }
    }

    fun isTemporary(path: String): Boolean = File(path).parentFile?.name == TEMP_DIR_NAME

    // Перенос временного файла в постоянное хранилище — вызывается при сохранении метки
    fun persist(path: String): String? {
        val source = File(path)
        if (!source.exists()) return null
        if (!isTemporary(path)) return path

        val target = File(photosDir, "photo_${UUID.randomUUID()}.jpg")
        return try {
            source.copyTo(target, overwrite = true)
            source.delete()
            target.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist photo", e)
            target.delete()
            null
        }
    }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (file.exists() && !file.delete()) {
            Log.e(TAG, "Failed to delete photo $path")
        }
    }

    fun exists(path: String?): Boolean = !path.isNullOrBlank() && File(path).exists()

    /**
     * Читает фото уменьшенным: снимок камеры целиком в память класть незачем,
     * а на больших фотографиях это ещё и OutOfMemoryError.
     */
    fun decodeScaled(path: String, maxSizePx: Int): Bitmap? {
        if (!File(path).exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= maxSizePx &&
            bounds.outHeight / (sampleSize * 2) >= maxSizePx
        ) {
            sampleSize *= 2
        }

        val bitmap = try {
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Not enough memory to decode photo $path", e)
            null
        } ?: return null

        return applyExifRotation(path, bitmap)
    }

    // Камера часто пишет снимок «как есть», а поворот телефона указывает в EXIF
    private fun applyExifRotation(path: String, bitmap: Bitmap): Bitmap {
        val degrees = try {
            when (ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read photo orientation", e)
            0f
        }
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    data class CameraOutput(val path: String, val uri: Uri)

    private companion object {
        const val TAG = "AttractionPhotoStorage"
        const val PHOTOS_DIR_NAME = "attraction_photos"
        const val TEMP_DIR_NAME = "attraction_photos_temp"
    }
}
