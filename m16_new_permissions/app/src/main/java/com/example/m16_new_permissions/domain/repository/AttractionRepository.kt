package com.example.m16_new_permissions.domain.repository

//import com.example.app.domain.model.Attraction
import com.example.m16_new_permissions.domain.model.Attraction

interface AttractionRepository {
    suspend fun getAttractions(): List<Attraction>

    // Добавление новой метки (например, по текущей геопозиции пользователя)
    suspend fun addAttraction(attraction: Attraction)

    // Сохранение изменений метки пользователя: названия, описания и координат
    suspend fun updateAttraction(attraction: Attraction)

    // Удаление метки пользователя по идентификатору
    suspend fun deleteAttraction(attractionId: String)
}
