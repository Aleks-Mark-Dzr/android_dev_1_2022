package com.example.m16_architecture.data

import android.util.Log
import com.example.m16_architecture.entity.FactsAboutNumbers
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URL
import javax.inject.Inject

interface NumbersApiService {
    @GET("random/math")
    suspend fun getRandomMathFact(
        @Query("json") json: Boolean = true
    ): FactsAboutNumbersDto
}

class FactsAboutNumbersRepository @Inject constructor(
    private val apiService: NumbersApiService
) {
    suspend fun getFactsAboutNumbers(): FactsAboutNumbers? {

        return try {
            Log.d("Repository", "Попытка получить факт...")
            val result = apiService.getRandomMathFact()
            Log.d("Repository", "Получен факт: ${result.text}")
            result
        } catch (jsonException: com.google.gson.JsonSyntaxException) {
            Log.e("Repository", "Ошибка парсинга JSON", jsonException)
            null
        } catch (e: Exception) {
            Log.e("Repository", "Ошибка при получении или парсинге фактов", e)
            null
        }
    }
}
