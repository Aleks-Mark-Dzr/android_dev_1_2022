package com.example.m17_new_firebase.data.repository

import com.example.m17_new_firebase.domain.model.Attraction
import com.example.m17_new_firebase.domain.repository.AttractionRepository


class AttractionRepositoryImpl : AttractionRepository {

    override suspend fun getAttractions(): List<Attraction> {
        // Если используем API, здесь будет запрос к серверу
        return listOf(
            Attraction("Eiffel Tower", "An iconic landmark of Paris.", 48.8584, 2.2945),
            Attraction("Statue of Liberty", "A symbol of freedom in the USA.", 40.6892, -74.0445),
            Attraction("Great Wall of China", "Historic wall across northern China.", 40.4319, 116.5704),
            Attraction("Shukhov Tower", "Опора ЛЭП НиГРЭС конструкции инженера В.Г. Шухова, 1927-1929 гг.", 56.193425, 43.543445),
            Attraction("Bear Land", "выставка декоративно-прикладного творчества Народного мастера Архангельской области Николая Фомина.", 61.490180, 38.926759)
            // Другие достопримечательности
        )
    }
}