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
import java.io.InputStream
import java.util.UUID

/**
 * Хранит фотографии меток в личной папке приложения.
 *
 * Снимок камеры и файл из галереи сначала попадают во временную папку в кэше: пока пользователь
 * не сохранил метку, постоянное место фото не занимает. При сохранении метки файл переносится
 * в filesDir — путь к нему остаётся рабочим и после перезапуска приложения, в отличие от
 * content-ссылки на галерею, разрешение на которую живёт только до перезагрузки.
 *
 * Наружу отдаём имя файла, а не полный путь: абсолютный путь зависит от устройства и профиля
 * пользователя, поэтому в резервной копии он бесполезен.
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
        return CameraOutput(file.absolutePath, uriFor(file))
    }

    /**
     * content-ссылка на фотографию для отправки в другое приложение.
     * Обе папки с фотографиями объявлены в file_paths.xml, поэтому годится и снятый только что
     * снимок, и давно сохранённое фото метки.
     */
    fun shareUri(path: String): Uri? {
        val file = File(path)
        if (!file.exists()) return null

        return try {
            uriFor(file)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to share photo $path", e)
            null
        }
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

    // Фотография из резервной копии: распаковываем во временную папку, как снимок камеры
    fun writeToTemp(source: InputStream): String? {
        val file = File(tempDir, "restored_${UUID.randomUUID()}.jpg")
        return try {
            file.outputStream().use { output -> source.copyTo(output) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unpack photo", e)
            file.delete()
            null
        }
    }

    fun isTemporary(path: String): Boolean = File(path).parentFile?.name == TEMP_DIR_NAME

    /**
     * Переносит временный файл в постоянное хранилище и возвращает имя сохранённой фотографии.
     *
     * Заодно уменьшает снимок: с камеры приходит 3–5 МБ, а метке хватает картинки, которая
     * помещается в экран. Мелкие фотографии — это и место в памяти, и размер резервной копии.
     */
    fun persist(path: String): String? {
        val source = File(path)
        if (!source.exists()) return null

        val bitmap = decodeScaled(path, STORED_PHOTO_MAX_SIZE_PX) ?: return persistOriginal(path)
        val scaled = scaleDown(bitmap, STORED_PHOTO_MAX_SIZE_PX)
        val target = File(photosDir, newPhotoName())

        return try {
            target.outputStream().use { output ->
                scaled.compress(Bitmap.CompressFormat.JPEG, STORED_PHOTO_QUALITY, output)
            }
            source.delete()
            target.name
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist photo", e)
            target.delete()
            null
        } finally {
            scaled.recycle()
        }
    }

    /**
     * Переносит файл как есть — без пересжатия.
     * Нужен для фотографий из резервной копии: они уже уменьшены, второй проход JPEG только
     * ухудшит картинку.
     */
    fun persistOriginal(path: String): String? {
        val source = File(path)
        if (!source.exists()) return null

        val target = File(photosDir, newPhotoName())
        return try {
            source.copyTo(target, overwrite = true)
            source.delete()
            target.name
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist photo", e)
            target.delete()
            null
        }
    }

    /** Абсолютный путь сохранённой фотографии или null, если файла уже нет */
    fun pathOf(photoName: String?): String? {
        if (photoName.isNullOrBlank()) return null

        val file = File(photosDir, photoName)
        return if (file.exists()) file.absolutePath else null
    }

    fun exists(photoName: String?): Boolean = pathOf(photoName) != null

    /** Удаляет сохранённую фотографию метки по её имени */
    fun deletePhoto(photoName: String?) {
        if (photoName.isNullOrBlank()) return
        deleteFile(File(photosDir, photoName).absolutePath)
    }

    /** Удаляет файл по полному пути — так убираются временные снимки */
    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (file.exists() && !file.delete()) {
            Log.e(TAG, "Failed to delete photo $path")
        }
    }

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

    /**
     * Доводит картинку до нужного размера: inSampleSize умеет делить только вдвое,
     * поэтому после декодирования сторона может остаться заметно больше требуемой.
     */
    private fun scaleDown(bitmap: Bitmap, maxSizePx: Int): Bitmap {
        val longestSide = maxOf(bitmap.width, bitmap.height)
        if (longestSide <= maxSizePx) return bitmap

        val ratio = maxSizePx.toFloat() / longestSide
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true
        )
        if (scaled != bitmap) bitmap.recycle()
        return scaled
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

    private fun newPhotoName(): String = "photo_${UUID.randomUUID()}.jpg"

    private fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(appContext, "${appContext.packageName}$FILE_PROVIDER_SUFFIX", file)

    data class CameraOutput(val path: String, val uri: Uri)

    private companion object {
        const val TAG = "AttractionPhotoStorage"
        const val FILE_PROVIDER_SUFFIX = ".fileprovider"
        const val PHOTOS_DIR_NAME = "attraction_photos"
        const val TEMP_DIR_NAME = "attraction_photos_temp"
        // Сохраняем фотографию уменьшенной: этого хватает и для диалога, и для полноэкранного просмотра
        const val STORED_PHOTO_MAX_SIZE_PX = 1600
        const val STORED_PHOTO_QUALITY = 85
    }
}
