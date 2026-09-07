package com.example.m16_new_permissions.domain.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Attraction(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    // Метка, добавленная пользователем по текущей геопозиции
    val isUserAdded: Boolean = false,
    /**
     * Имена файлов фотографий в папке приложения; пустой список — фотографий нет.
     * Порядок задаёт пользователь: первая фотография показывается в списке первой.
     *
     * Храним имена, а не абсолютные пути: путь зависит от устройства и профиля пользователя,
     * поэтому в резервной копии он бесполезен.
     */
    val photoNames: List<String> = emptyList(),
    /**
     * Единственная фотография старого формата, когда к метке можно было приложить только одну.
     * Читается ради уже сохранённых меток и записей в старых резервных копиях: при загрузке
     * [withNormalizedPhotos] переносит её в [photoNames]. Раньше здесь лежал ещё и абсолютный
     * путь — он ломается при переносе на другое устройство, поэтому читаем его как псевдоним.
     */
    @SerializedName(value = "photoName", alternate = ["photoPath"])
    val legacyPhotoName: String? = null,
    // Идентификатор нужен, чтобы находить метку пользователя при редактировании и удалении
    val id: String = UUID.randomUUID().toString(),
    // Время последнего изменения: по нему восстановление из копии решает, чья версия свежее
    val updatedAt: Long = System.currentTimeMillis()
) {

    /**
     * Приводит фотографии записи к текущему формату: единственное фото старой метки становится
     * первым элементом списка, а от абсолютных путей остаётся только имя файла.
     *
     * Список приходит из Gson, поэтому его может не быть вовсе: в записи старого формата такого
     * поля нет, и Gson оставляет вместо списка null. Запись с ним обязательно надо переписать,
     * иначе на первом же обращении к фотографиям метки приложение упадёт.
     */
    fun withNormalizedPhotos(): Attraction {
        @Suppress("SENSELESS_COMPARISON")
        val stored = if (photoNames == null) null else photoNames.filterNotNull()
        val normalized = (listOfNotNull(legacyPhotoName) + stored.orEmpty())
            .map { it.substringAfterLast('/').substringAfterLast('\\') }
            .filter { it.isNotBlank() }
            .distinct()

        return if (legacyPhotoName == null && normalized == stored) this
        else copy(photoNames = normalized, legacyPhotoName = null)
    }
}
