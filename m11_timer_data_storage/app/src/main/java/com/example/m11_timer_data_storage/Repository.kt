package com.example.m11_timer_data_storage

import android.content.Context

class Repository(context: Context) {

    private val preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private var localVariable: String? = null

    private fun getDataFromSharedPreference(): String? = preferences.getString("data_key", null)

    private fun getDataFromLocalVariable(): String? = localVariable

    fun saveText(text: String) {
        localVariable = text
        preferences.edit().putString("data_key", text).apply()
    }

    fun clearText() {
        localVariable = null
        preferences.edit().remove("data_key").apply()
    }

    fun getText(): String = localVariable ?: getDataFromSharedPreference() ?: "No data"
}