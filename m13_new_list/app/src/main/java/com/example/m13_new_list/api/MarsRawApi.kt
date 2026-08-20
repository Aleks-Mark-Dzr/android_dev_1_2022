package com.example.m13_new_list.api

import com.example.m13_new_list.models.MslRawImagesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Сырые кадры марсоходов: https://mars.nasa.gov/api/v1/ */
interface MarsRawApi {

    /**
     * Снимки марсохода за конкретный сол.
     * Диапазон задаётся парой условий gte/lte — точечного фильтра по солу у API нет.
     * Ключ не требуется.
     */
    @GET("raw_image_items/")
    suspend fun getPhotosBySol(
        @Query("condition_2") solFrom: String,
        @Query("condition_3") solTo: String,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE,
        @Query("page") page: Int = 0,
        @Query("condition_1") mission: String = MISSION_CURIOSITY,
        @Query("order") order: String = DEFAULT_ORDER
    ): MslRawImagesResponse

    companion object {
        const val BASE_URL = "https://mars.nasa.gov/api/v1/"
        const val DEFAULT_PER_PAGE = 50
        private const val MISSION_CURIOSITY = "msl:mission"
        private const val DEFAULT_ORDER = "sol desc,date_taken desc"

        fun solCondition(sol: Int, comparison: String) = "$sol:sol:$comparison"
    }
}
