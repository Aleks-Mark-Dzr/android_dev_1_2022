package com.example.m16_new_permissions.domain.model

import java.util.UUID

data class Attraction(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    // Метка, добавленная пользователем по текущей геопозиции
    val isUserAdded: Boolean = false,
    // Путь к фотографии метки во внутреннем хранилище приложения, null — фото не добавлено
    val photoPath: String? = null,
    // Идентификатор нужен, чтобы находить метку пользователя при редактировании и удалении
    val id: String = UUID.randomUUID().toString()
)
