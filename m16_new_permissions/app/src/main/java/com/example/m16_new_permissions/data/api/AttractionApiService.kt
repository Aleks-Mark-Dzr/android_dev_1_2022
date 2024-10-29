package com.example.m16_new_permissions.data.api

import com.example.m16_new_permissions.domain.model.Attraction
import retrofit2.http.GET
import retrofit2.http.Query

interface AttractionApiService {
    @GET("radius")
    suspend fun getAttractions(
        @Query("apikey") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius") radius: Int = 1000,  // Радиус поиска в метрах
        @Query("limit") limit: Int = 20,      // Количество результатов
        @Query("rate") rate: String = "2",    // Популярность места
        @Query("format") format: String = "json"
    ): List<Attraction>
}