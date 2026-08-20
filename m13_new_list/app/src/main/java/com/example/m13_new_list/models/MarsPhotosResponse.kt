package com.example.m13_new_list.models

data class Photo(
    val id: Int,
    val sol: Int,
    val img_src: String,
    val earth_date: String,
    val rover: Rover,
    val camera: Camera
)

data class Rover(
    val name: String
)

data class Camera(
    val name: String,
    val full_name: String
)
