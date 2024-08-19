package com.example.m16_architecture.data

import android.util.Log
import com.example.m16_architecture.entity.FactsAboutNumbers
import com.google.gson.Gson
import retrofit2.http.GET
import javax.inject.Inject

interface NumbersApiService {
    @GET("random/math")
//    suspend fun getRandomMathFact(): FactsAboutNumbersDto
    suspend fun getRandomMathFactRaw(): String
}

class FactsAboutNumbersRepository @Inject constructor(
    private val apiService: NumbersApiService
) {
    suspend fun getFactsAboutNumbers(): FactsAboutNumbers? {
        return try {
            val rawResult = apiService.getRandomMathFactRaw()
            Log.d("Repository", "Raw API response: $rawResult")
//            val result = apiService.getRandomMathFact()
//            Log.d("Repository", "Fetched fact: ${result.text}")
//            result
            // Простая проверка того, что в JSON присутствует ожидаемый ключ
            if (rawResult.isNotEmpty() && rawResult.contains("\"text\"")) {
                val gson = Gson()
                val fact = gson.fromJson(rawResult, FactsAboutNumbersDto::class.java)
                Log.d("Repository", "Parsed fact: $fact")
                fact
            } else {
                Log.e("Repository", "Unexpected JSON format or empty response")
                null
            }
        } catch (jsonException: com.google.gson.JsonSyntaxException) {
            Log.e("Repository", "JSON parsing error", jsonException)
            null
        }catch (e: Exception) {
            Log.e("Repository", "Error fetching or parsing facts", e)
            null
        }
    }
}