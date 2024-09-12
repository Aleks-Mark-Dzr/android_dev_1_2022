package com.example.m13_new_list.api

import com.example.m13_new_list.models.MarsPhotosResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MarsApi {
    @GET("rovers/curiosity/photos")
    suspend fun getMarsPhotos(
        @Query("sol") sol: Int,
        @Query("api_key") apiKey: String
    ): MarsPhotosResponse
}