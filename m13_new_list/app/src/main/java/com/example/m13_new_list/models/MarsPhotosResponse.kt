package com.example.m13_new_list.models


data class MarsPhotosResponse(
    val photos: List<Photo>
)

data class Photo(
    val id: Int,
    val sol: Int,
    val img_src: String,
    val earth_date: String,
    val rover: Rover,
    val camera: Camera
)

data class Rover(
    val id: Int,
    val name: String,
    val landing_date: String,
    val launch_date: String,
    val status: String
)

data class Camera(
    val id: Int,
    val name: String,
    val full_name: String
)