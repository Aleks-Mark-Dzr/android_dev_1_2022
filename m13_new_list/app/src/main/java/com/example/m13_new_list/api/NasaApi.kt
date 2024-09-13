package com.example.m13_new_list.api

//class NasaApi {
//}

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/"

    // Функция для создания OkHttpClient с interceptor
    private fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // или уровень по вашему выбору
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging) // добавляем interceptor
            .build()
    }

    val api: MarsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarsApi::class.java)
    }
}