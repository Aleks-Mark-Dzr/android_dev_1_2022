package com.example.m16_architecture.data

//import com.example.m16_architecture.entity.UsefulActivity
//import com.google.gson.annotations.SerializedName
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.GET
//import javax.inject.Inject
//import javax.inject.Singleton
//
//interface BoredApiService {
//    @GET("api/activity")
//    suspend fun getActivity(): UsefulActivityDto
//}
//
//@Singleton
//class UsefulActivitiesRepository @Inject constructor() {
//    private val retrofit = Retrofit.Builder()
//        .baseUrl("http://numbersapi.com/#random/math")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    private val apiService = retrofit.create(BoredApiService::class.java)
//
//    suspend fun getUsefulActivity(): UsefulActivity {
//        return apiService.getActivity()
//    }
//}

import javax.inject.Inject

interface UsefulActivitiesRepository {
    suspend fun getUsefulActivity(): UsefulActivity
}

class UsefulActivitiesRepositoryImpl @Inject constructor(
    private val apiService: NumbersApiService
) : UsefulActivitiesRepository {
    override suspend fun getUsefulActivity(): UsefulActivity {
        return apiService.getRandomMathFact()
    }
}
