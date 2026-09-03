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
     * Имя файла фотографии в папке приложения, null — фото не добавлено.
     * Раньше здесь лежал абсолютный путь: он ломается при переносе на другое устройство,
     * поэтому старое поле читаем как псевдоним и превращаем в имя файла при загрузке.
     */
    @SerializedName(value = "photoName", alternate = ["photoPath"])
    val photoName: String? = null,
    // Идентификатор нужен, чтобы находить метку пользователя при редактировании и удалении
    val id: String = UUID.randomUUID().toString(),
    // Время последнего изменения: по нему восстановление из копии решает, чья версия свежее
    val updatedAt: Long = System.currentTimeMillis()
)
