package com.example.m14_retrofit

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://randomuser.me/api/"

object RetrofitService {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val searchRandomuserApi: SearchRandomuserApi = retrofit.create(
        SearchRandomuserApi::class.java
    )
}

interface SearchRandomuserApi {
    @GET("/api/")
    suspend fun getRandomUser(): Response<UserResponse>

}