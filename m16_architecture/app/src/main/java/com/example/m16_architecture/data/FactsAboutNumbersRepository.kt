package com.example.m16_architecture.data

import android.util.Log
import com.example.m16_architecture.entity.FactsAboutNumbers
import retrofit2.http.GET
import javax.inject.Inject

interface NumbersApiService {
    @GET("random/math")
    suspend fun getRandomMathFact(): FactsAboutNumbersDto
}

class FactsAboutNumbersRepository @Inject constructor(
    private val apiService: NumbersApiService
) {
    suspend fun getFactsAboutNumbers(): FactsAboutNumbers? {
        return try {
            apiService.getRandomMathFact()
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching facts", e)
            null
        }
    }
}