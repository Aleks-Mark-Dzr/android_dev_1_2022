package com.example.m13_new_list.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Функция для создания OkHttpClient с interceptor
    private fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // или уровень по вашему выбору
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging) // добавляем interceptor
            .build()
    }

    // У источников разные хосты, поэтому Retrofit нужен свой на каждый.
    // Клиент общий: пул соединений и кэш переиспользуются.
    private val client by lazy { createOkHttpClient() }

    private fun <T> createApi(baseUrl: String, service: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)

    val rawApi: MarsRawApi by lazy {
        createApi(MarsRawApi.BASE_URL, MarsRawApi::class.java)
    }

    val imagesApi: NasaImagesApi by lazy {
        createApi(NasaImagesApi.BASE_URL, NasaImagesApi::class.java)
    }
}
