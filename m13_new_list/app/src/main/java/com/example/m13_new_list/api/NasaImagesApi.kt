package com.example.m13_new_list.api

import com.example.m13_new_list.models.NasaImagesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Медиабиблиотека NASA: https://images-api.nasa.gov/ */
interface NasaImagesApi {

    /**
     * Поиск обработанных снимков.
     * Фильтры keywords/center отсекают снимки самих аппаратов на Земле,
     * оставляя марсианскую съёмку. Ключ не требуется.
     */
    @GET("search")
    suspend fun searchPhotos(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = DEFAULT_PAGE_SIZE,
        @Query("media_type") mediaType: String = MEDIA_TYPE_IMAGE,
        @Query("keywords") keywords: String = KEYWORD_MARS,
        @Query("center") center: String = CENTER_JPL
    ): NasaImagesResponse

    companion object {
        const val BASE_URL = "https://images-api.nasa.gov/"
        const val DEFAULT_PAGE_SIZE = 50
        private const val MEDIA_TYPE_IMAGE = "image"
        private const val KEYWORD_MARS = "Mars"
        private const val CENTER_JPL = "JPL"
    }
}
