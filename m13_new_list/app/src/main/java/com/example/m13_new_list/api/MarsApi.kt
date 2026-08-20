package com.example.m13_new_list.api

import com.example.m13_new_list.models.MslRawImagesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MarsApi {

    /**
     * Снимки марсохода за конкретный сол.
     * Диапазон задаётся парой условий gte/lte — точечного фильтра по солу у API нет.
     * Ключ не требуется.
     */
    @GET("raw_image_items/")
    suspend fun getMarsPhotos(
        @Query("condition_2") solFrom: String,
        @Query("condition_3") solTo: String,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE,
        @Query("page") page: Int = 0,
        @Query("condition_1") mission: String = MISSION_CURIOSITY,
        @Query("order") order: String = DEFAULT_ORDER
    ): MslRawImagesResponse

    companion object {
        const val MISSION_CURIOSITY = "msl:mission"
        const val DEFAULT_PER_PAGE = 50
        private const val DEFAULT_ORDER = "sol desc,date_taken desc"

        fun solCondition(sol: Int, comparison: String) = "$sol:sol:$comparison"
    }
}
