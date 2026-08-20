package com.example.m13_new_list

import com.example.m13_new_list.models.MslRawImagesResponse
import com.example.m13_new_list.models.toPhoto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MslRawMapperTest {

    private val photos = Gson()
        .fromJson(readSample(), MslRawImagesResponse::class.java)
        .items
        .mapNotNull { it.toPhoto() }

    @Test
    fun `кадры без ссылки на изображение отбрасываются`() {
        assertEquals(2, photos.size)
        assertTrue(photos.none { it.id == "260108" })
    }

    @Test
    fun `поля кадра разбираются целиком`() {
        val photo = photos.first()
        assertEquals("260106", photo.id)
        assertEquals("Curiosity", photo.rover)
        assertEquals("1000", photo.sol)
        assertEquals("Mast Camera (Mastcam)", photo.camera)
        assertEquals("2015-05-30", photo.date)
        assertEquals("NASA/JPL-Caltech/MSSS", photo.credit)
    }

    @Test
    fun `превью и оригинал у сырого кадра совпадают`() {
        val photo = photos.first()
        assertEquals(photo.previewUrl, photo.fullUrl)
        assertTrue(photo.previewUrl.startsWith("https://"))
    }

    @Test
    fun `без https_url ссылка берётся из url и поднимается до https`() {
        assertEquals(
            "https://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/only-http.jpg",
            photos[1].previewUrl
        )
    }

    private fun readSample(): String =
        javaClass.classLoader!!.getResourceAsStream("msl_raw_sample.json")!!
            .bufferedReader()
            .use { it.readText() }
}
