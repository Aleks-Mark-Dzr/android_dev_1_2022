package com.example.m16_new_permissions

import com.example.m16_new_permissions.domain.model.Attraction
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Метки, сохранённые прежними версиями приложения, читаются тем же Gson, что и новые.
 * Поля со списком фотографий в них нет, и Gson оставляет его пустым — на такой записи
 * ничего не должно падать.
 */
class AttractionPhotoMigrationTest {

    private val gson = Gson()

    @Test
    fun `record without photo fields has empty photo list`() {
        val attraction = parse(
            """{"name":"Старая метка","description":"","latitude":1.0,"longitude":2.0,
               "isUserAdded":true,"id":"1","updatedAt":1}"""
        )

        assertEquals(emptyList<String>(), attraction.photoNames)
    }

    @Test
    fun `single photo of the old format moves to the list`() {
        val attraction = parse(
            """{"name":"Метка с фото","description":"","latitude":1.0,"longitude":2.0,
               "isUserAdded":true,"photoName":"photo_1.jpg","id":"2","updatedAt":1}"""
        )

        assertEquals(listOf("photo_1.jpg"), attraction.photoNames)
    }

    @Test
    fun `absolute path of the oldest format turns into a file name`() {
        val attraction = parse(
            """{"name":"Метка с путём","description":"","latitude":1.0,"longitude":2.0,
               "isUserAdded":true,"photoPath":"/data/user/0/app/files/attraction_photos/photo_2.jpg",
               "id":"3","updatedAt":1}"""
        )

        assertEquals(listOf("photo_2.jpg"), attraction.photoNames)
    }

    @Test
    fun `several photos keep the order they were added in`() {
        val attraction = parse(
            """{"name":"Метка с альбомом","description":"","latitude":1.0,"longitude":2.0,
               "isUserAdded":true,"photoNames":["photo_1.jpg","photo_2.jpg"],
               "id":"4","updatedAt":1}"""
        )

        assertEquals(listOf("photo_1.jpg", "photo_2.jpg"), attraction.photoNames)
    }

    private fun parse(json: String): Attraction =
        gson.fromJson(json, Attraction::class.java).withNormalizedPhotos()
}
