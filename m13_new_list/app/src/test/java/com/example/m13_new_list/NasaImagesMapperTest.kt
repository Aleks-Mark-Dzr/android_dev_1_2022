package com.example.m13_new_list

import com.example.m13_new_list.models.NasaImagesResponse
import com.example.m13_new_list.models.toPhoto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NasaImagesMapperTest {

    private val photos = Gson()
        .fromJson(readSample(), NasaImagesResponse::class.java)
        .collection!!
        .items
        .mapNotNull { it.toPhoto() }

    @Test
    fun `элементы без картинки и без данных отбрасываются`() {
        assertEquals(2, photos.size)
        assertTrue(photos.none { it.id == "BROKEN" })
    }

    @Test
    fun `поля снимка разбираются целиком`() {
        val photo = photos.first()
        assertEquals("PIA22327", photo.id)
        assertEquals("Inlet Cover On the Curiosity Rover", photo.title)
        assertEquals("Curiosity", photo.rover)
        assertEquals("2068", photo.sol)
        assertEquals("2018-06-04", photo.date)
        assertEquals("NASA/JPL-Caltech/MSSS", photo.credit)
    }

    @Test
    fun `ссылка на каталог из описания вырезается`() {
        assertTrue(photos.first().description.endsWith("(Mastcam)."))
    }

    @Test
    fun `превью поднимается до https, оригинал берётся из canonical`() {
        val photo = photos.first()
        assertEquals(
            "https://images-assets.nasa.gov/image/PIA22327/PIA22327~thumb.jpg",
            photo.previewUrl
        )
        assertEquals(
            "https://images-assets.nasa.gov/image/PIA22327/PIA22327~orig.jpg",
            photo.fullUrl
        )
    }

    @Test
    fun `без canonical оригиналом становится превью, а credit падает на center`() {
        val photo = photos[1]
        assertEquals(photo.previewUrl, photo.fullUrl)
        assertEquals("JPL", photo.credit)
        assertEquals("Perseverance", photo.rover)
        assertEquals("", photo.sol)
    }

    private fun readSample(): String =
        javaClass.classLoader!!.getResourceAsStream("nasa_images_sample.json")!!
            .bufferedReader()
            .use { it.readText() }
}
