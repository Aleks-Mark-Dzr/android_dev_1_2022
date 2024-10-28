package com.example.m16_new_permissions.data.repository

import com.example.m16_new_permissions.domain.model.Attraction
import com.example.m16_new_permissions.domain.repository.AttractionRepository


class AttractionRepositoryImpl : AttractionRepository {

    override suspend fun getAttractions(): List<Attraction> {
        // Если используем API, здесь будет запрос к серверу
        return listOf(
            Attraction("Eiffel Tower", "An iconic landmark of Paris.", 48.8584, 2.2945),
            Attraction("Statue of Liberty", "A symbol of freedom in the USA.", 40.6892, -74.0445),
            Attraction("Great Wall of China", "Historic wall across northern China.", 40.4319, 116.5704)
            // Другие достопримечательности
        )
    }
}