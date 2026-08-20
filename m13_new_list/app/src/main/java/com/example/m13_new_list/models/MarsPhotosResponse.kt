package com.example.m13_new_list.models

/**
 * Общая модель снимка для обоих источников.
 * Часть полей есть только у одного из них — там, где данных нет, остаётся пустая строка.
 */
data class Photo(
    val id: String,
    val title: String,
    val description: String,
    val credit: String,
    val rover: String,
    val sol: String,
    val camera: String,
    val date: String,
    val previewUrl: String,
    val fullUrl: String
)
