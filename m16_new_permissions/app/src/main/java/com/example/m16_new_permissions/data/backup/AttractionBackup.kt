package com.example.m16_new_permissions.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.m16_new_permissions.data.local.AttractionLocalDataSource
import com.example.m16_new_permissions.data.local.AttractionPhotoStorage
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.model.RestoreSummary
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Резервная копия меток пользователя в виде zip-архива.
 *
 * Архив пишется в файл, который пользователь сам выбрал системным диалогом, — значит его можно
 * положить в Google Диск, любое другое облако или просто отправить себе. Приложению для этого
 * не нужны ни разрешения на хранилище, ни доступ к аккаунту.
 *
 * Структура архива:
 * ```
 * attractions.json          — версия формата и список меток
 * photos/photo_<uuid>.jpg   — фотографии, по файлу на метку
 * ```
 */
class AttractionBackup(
    context: Context,
    private val localDataSource: AttractionLocalDataSource,
    private val photoStorage: AttractionPhotoStorage
) {

    private val resolver = context.applicationContext.contentResolver
    private val gson = Gson()

    /** Записывает метки и их фотографии в выбранный пользователем файл */
    fun export(target: Uri): Result<Int> {
        val attractions = localDataSource.getUserAttractions()

        return try {
            val output = resolver.openOutputStream(target)
                ?: return Result.failure(IOException("Cannot open $target for writing"))

            output.use { raw ->
                ZipOutputStream(raw.buffered()).use { zip ->
                    val payload = BackupPayload(FORMAT_VERSION, System.currentTimeMillis(), attractions)
                    zip.putNextEntry(ZipEntry(ENTRY_ATTRACTIONS))
                    zip.write(gson.toJson(payload).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()

                    attractions.forEach { attraction ->
                        val photoName = attraction.photoName ?: return@forEach
                        // Фотографию могли удалить извне — тогда метка уедет в копию без неё
                        val photoPath = photoStorage.pathOf(photoName) ?: return@forEach

                        zip.putNextEntry(ZipEntry("$PHOTOS_DIR/$photoName"))
                        File(photoPath).inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
            Result.success(attractions.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export attractions", e)
            Result.failure(e)
        }
    }

    /**
     * Восстанавливает метки из архива, не стирая имеющиеся: новые добавляются, а для совпавших
     * по id берётся та версия, которую изменили позже. Так копию можно разворачивать на телефон,
     * где уже что-то наотмечали, и ничего не потерять.
     */
    fun import(source: Uri): Result<RestoreSummary> {
        // Фотографии распаковываем во временную папку: какие из них нужны, станет ясно после разбора JSON
        val unpackedPhotos = mutableMapOf<String, String>()

        return try {
            val input = resolver.openInputStream(source)
                ?: return Result.failure(IOException("Cannot open $source for reading"))

            var payload: BackupPayload? = null
            input.use { raw ->
                ZipInputStream(raw.buffered()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        readEntry(entry, zip, unpackedPhotos)?.let { payload = it }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            val backup = payload
                ?: return Result.failure(IOException("Archive has no $ENTRY_ATTRACTIONS"))
            if (backup.formatVersion > FORMAT_VERSION) {
                return Result.failure(IOException("Unsupported backup version ${backup.formatVersion}"))
            }

            Result.success(merge(backup.attractions.orEmpty(), unpackedPhotos))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import attractions", e)
            Result.failure(e)
        } finally {
            // Фотографии отменённых и пропущенных меток остаются в кэше мусором
            unpackedPhotos.values.forEach(photoStorage::deleteFile)
        }
    }

    // Читает одну запись архива: возвращает разобранный список меток, если это был JSON
    private fun readEntry(
        entry: ZipEntry,
        zip: ZipInputStream,
        unpackedPhotos: MutableMap<String, String>
    ): BackupPayload? {
        if (entry.isDirectory) return null

        return when {
            entry.name == ENTRY_ATTRACTIONS -> try {
                gson.fromJson(zip.readBytes().toString(Charsets.UTF_8), BackupPayload::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Malformed $ENTRY_ATTRACTIONS in backup", e)
                null
            }

            isPhotoEntry(entry.name) -> {
                val photoName = entry.name.substringAfter('/')
                photoStorage.writeToTemp(zip)?.let { unpackedPhotos[photoName] = it }
                null
            }

            else -> null
        }
    }

    /**
     * Пускаем только фотографии из своей папки и с безопасным именем: имя записи в архиве
     * приходит извне, и путь вида `../../` увёл бы распаковку за пределы приложения.
     */
    private fun isPhotoEntry(entryName: String): Boolean {
        if (!entryName.startsWith("$PHOTOS_DIR/")) return false

        val photoName = entryName.substringAfter('/')
        return photoName.isNotEmpty() && photoName.all { it.isLetterOrDigit() || it in "._-" }
    }

    private fun merge(
        imported: List<Attraction>,
        unpackedPhotos: Map<String, String>
    ): RestoreSummary {
        val current = localDataSource.getUserAttractions()
        val result = current.toMutableList()
        // Одна и та же фотография не должна перекладываться в хранилище дважды
        val storedPhotos = mutableMapOf<String, String>()

        var added = 0
        var updated = 0
        var skipped = 0

        imported.forEach { candidate ->
            val existingIndex = result.indexOfFirst { it.id == candidate.id }
            val existing = result.getOrNull(existingIndex)
            if (existing != null && candidate.updatedAt <= existing.updatedAt) {
                skipped++
                return@forEach
            }

            // Метка без фотографии в архиве восстанавливается без неё — путь из чужого телефона не годится
            val photoName = candidate.photoName?.let { archiveName ->
                storedPhotos[archiveName] ?: unpackedPhotos[archiveName]
                    ?.let(photoStorage::persistOriginal)
                    ?.also { storedPhotos[archiveName] = it }
            }
            // Восстановленная метка всегда пользовательская: предустановленные заданы в коде
            val restored = candidate.copy(isUserAdded = true, photoName = photoName)

            if (existing == null) {
                result += restored
                added++
            } else {
                result[existingIndex] = restored
                if (existing.photoName != null && existing.photoName != photoName) {
                    photoStorage.deletePhoto(existing.photoName)
                }
                updated++
            }
        }

        localDataSource.saveUserAttractions(result)
        return RestoreSummary(added, updated, skipped)
    }

    /**
     * Содержимое attractions.json. Поля объявлены допускающими null: файл приходит извне,
     * и подсовывать в него можно что угодно.
     */
    private data class BackupPayload(
        val formatVersion: Int,
        val exportedAt: Long,
        val attractions: List<Attraction>?
    )

    private companion object {
        const val TAG = "AttractionBackup"
        const val FORMAT_VERSION = 1
        const val ENTRY_ATTRACTIONS = "attractions.json"
        const val PHOTOS_DIR = "photos"
    }
}
