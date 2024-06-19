package com.example.m14_retrofit

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("gender") val gender: String,
    val name: Name,
    val location: Location,
    @SerializedName("email") val email: String,
    val login: Login,
    val dob: Dob,
    val registered: Registered,
    @SerializedName("phone") val phone: String,
    @SerializedName("cell") val cell: String,
    val id: Id,
    val picture: Picture,
    @SerializedName("nat") val nat: String
)

data class Name(
    @SerializedName("title") val title: String,
    @SerializedName("first") val first: String,
    @SerializedName("last") val last: String
)

data class Location(
    val street: Street,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("country") val country: String,
    @SerializedName("postcode") val postcode: Int,
    val coordinates: Coordinates,
    val timezone: Timezone
)

data class Street(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String
)

data class Coordinates(
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String
)

data class Timezone(
    @SerializedName("offset") val offset: String,
    @SerializedName("description") val description: String
)

data class Login(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("salt") val salt: String,
    @SerializedName("md5") val md5: String,
    @SerializedName("sha1") val sha1: String,
    @SerializedName("sha256") val sha256: String
)

data class Dob(
    @SerializedName("date") val date: String,
    @SerializedName("age") val age: Int
)

data class Registered(
    @SerializedName("date") val date: String,
    @SerializedName("age") val age: Int
)

data class Id(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?
)

data class Picture(
    @SerializedName("large") val large: String,
    @SerializedName("medium") val medium: String,
    @SerializedName("thumbnail") val thumbnail: String
)

data class UserResponse(
    val results: List<User>,
    val info: Info
)

data class Info(
    @SerializedName("seed") val seed: String,
    @SerializedName("results") val results: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("version") val version: String
)
