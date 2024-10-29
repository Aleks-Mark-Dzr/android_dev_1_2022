package com.example.m16_new_permissions.data.repository

import com.example.m16_new_permissions.data.api.AttractionApiService
import com.example.m16_new_permissions.domain.model.Attraction

class AttractionRepositoryImpl(private val apiService: AttractionApiService) {
    private val apiKey = "5ae2e3f221c38a28845f05b69876573332b8bccd746e2f0fd084fa5e"  // API-ключ OpenTripMap

    suspend fun getAttractions(latitude: Double, longitude: Double): List<Attraction> {
        return apiService.getAttractions(apiKey, latitude, longitude)
    }
}