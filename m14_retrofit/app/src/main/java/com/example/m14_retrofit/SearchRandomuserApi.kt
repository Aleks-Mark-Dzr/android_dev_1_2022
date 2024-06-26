package com.example.m14_retrofit

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://randomuser.me/api/"

object RetrofitService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val searchRandomuserApi: SearchRandomuserApi = retrofit.create(
        SearchRandomuserApi::class.java
    )
}

interface SearchRandomuserApi {
    @GET("/api/")
    suspend fun getRandomUser(): Response<UserResponse>
}
