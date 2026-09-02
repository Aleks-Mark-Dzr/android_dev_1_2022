package com.example.m16_new_permissions.data.repository

import com.example.m16_new_permissions.data.local.AttractionLocalDataSource
import com.example.m16_new_permissions.data.local.AttractionPhotoStorage
import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.repository.AttractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AttractionRepositoryImpl(
    private val localDataSource: AttractionLocalDataSource,
    private val photoStorage: AttractionPhotoStorage
) : AttractionRepository {

    // Если используем API, здесь будет запрос к серверу
    private val defaultAttractions = listOf(
        Attraction("Eiffel Tower", "An iconic landmark of Paris.", 48.8584, 2.2945),
        Attraction("Statue of Liberty", "A symbol of freedom in the USA.", 40.6892, -74.0445),
        Attraction("Great Wall of China", "Historic wall across northern China.", 40.4319, 116.5704),
        Attraction("Shukhov Tower", "Опора ЛЭП НиГРЭС конструкции инженера В.Г. Шухова, 1927-1929 гг.", 56.193425, 43.543445),
        Attraction("Bear Land", "выставка декоративно-прикладного творчества Народного мастера Архангельской области Николая Фомина.", 61.490180, 38.926759),
        Attraction("Всероссийский Черноморский Тангокемп Caminito!", "С 2011 года с 01 по 31 июля в Совет-Квадже мы танцуем танго", 43.988548, 39.220239),
        Attraction("Адрес квартиры в Пафосе любимой дочери Лилии", "Eleutheriou Chandrinou 5, Lighthouse complex, apartament\n" +
                "b205, 8045, Paphos", 34.765262, 32.409283)
        // Другие достопримечательности
    )

    // К предустановленным достопримечательностям добавляем сохранённые метки пользователя
    override suspend fun getAttractions(): List<Attraction> = withContext(Dispatchers.IO) {
        defaultAttractions + localDataSource.getUserAttractions()
    }

    override suspend fun addAttraction(attraction: Attraction) = withContext(Dispatchers.IO) {
        localDataSource.saveUserAttractions(localDataSource.getUserAttractions() + attraction)
    }

    // Редактировать и удалять можно только метки пользователя: предустановленные заданы в коде
    override suspend fun updateAttraction(attraction: Attraction) = withContext(Dispatchers.IO) {
        val updated = localDataSource.getUserAttractions().map { saved ->
            if (saved.id == attraction.id) attraction else saved
        }
        localDataSource.saveUserAttractions(updated)
    }

    override suspend fun deleteAttraction(attractionId: String) = withContext(Dispatchers.IO) {
        val saved = localDataSource.getUserAttractions()
        // Вместе с меткой удаляем и её фотографию, иначе файл останется висеть во внутренней памяти
        saved.firstOrNull { it.id == attractionId }?.let { photoStorage.delete(it.photoPath) }
        localDataSource.saveUserAttractions(saved.filterNot { it.id == attractionId })
    }
}
