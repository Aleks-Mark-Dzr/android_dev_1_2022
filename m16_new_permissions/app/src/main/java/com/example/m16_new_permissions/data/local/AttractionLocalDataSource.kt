package com.example.m16_new_permissions.data.local

import android.content.Context
import android.util.Log
import com.example.m16_new_permissions.domain.model.Attraction
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Хранит метки, добавленные пользователем, в SharedPreferences в виде JSON,
 * чтобы они не пропадали между запусками приложения.
 */
class AttractionLocalDataSource(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()
    private val attractionListType = object : TypeToken<List<Attraction>>() {}.type

    fun getUserAttractions(): List<Attraction> {
        val json = preferences.getString(KEY_USER_ATTRACTIONS, null) ?: return emptyList()
        return try {
            val saved = gson.fromJson<List<Attraction>>(json, attractionListType) ?: emptyList()
            // Метки, сохранённые до появления id, Gson отдаёт без идентификатора:
            // выдаём его сами, иначе такую метку нельзя отредактировать или удалить
            val withIds = saved.map { attraction ->
                if (attraction.id.isNullOrBlank()) {
                    attraction.copy(id = UUID.randomUUID().toString())
                } else {
                    attraction
                }
            }
            if (withIds != saved) {
                saveUserAttractions(withIds)
            }
            withIds
        } catch (e: JsonSyntaxException) {
            // Сохранённые данные повреждены — сбрасываем их, чтобы приложение не падало на каждом запуске
            Log.e("AttractionLocalDS", "Failed to parse saved attractions, clearing storage", e)
            preferences.edit().remove(KEY_USER_ATTRACTIONS).apply()
            emptyList()
        }
    }

    fun saveUserAttractions(attractions: List<Attraction>) {
        preferences.edit()
            .putString(KEY_USER_ATTRACTIONS, gson.toJson(attractions, attractionListType))
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "attractions_prefs"
        const val KEY_USER_ATTRACTIONS = "user_attractions"
    }
}
