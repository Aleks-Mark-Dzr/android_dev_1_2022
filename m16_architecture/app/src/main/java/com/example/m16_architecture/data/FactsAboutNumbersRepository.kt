package com.example.m16_architecture.data

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
    suspend fun getFactsAboutNumbers(): FactsAboutNumbers {
        return apiService.getRandomMathFact()
    }
}