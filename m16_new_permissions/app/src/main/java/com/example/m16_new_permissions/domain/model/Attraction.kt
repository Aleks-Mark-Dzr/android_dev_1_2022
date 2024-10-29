package com.example.m16_new_permissions.domain.model

data class Attraction(
    val xid: String,          // Уникальный идентификатор достопримечательности
    val name: String,         // Название
    val dist: Double,         // Расстояние от точки поиска
    val point: Point          // Координаты
)

data class Point(
    val lat: Double,
    val lon: Double
)