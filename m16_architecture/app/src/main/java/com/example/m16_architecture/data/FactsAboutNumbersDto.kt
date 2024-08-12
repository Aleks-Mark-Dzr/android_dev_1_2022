package com.example.m16_architecture.data

//import com.example.m16_architecture.entity.UsefulActivity
//import com.google.gson.annotations.SerializedName
//
//data class UsefulActivityDto(
////    @SerializedName("activity") override val activity: String,
//    @SerializedName("type") override val type: String,
////    @SerializedName("participants") override val participants: Int,
////    @SerializedName("price") override val price: Double,
////    @SerializedName("link") override val link: String,
////    @SerializedName("key") override val key: String,
////    @SerializedName("accessibility") override val accessibility: Double,
//    @SerializedName("text") override val text: String,
//    @SerializedName("found") override val found: Boolean,
//    @SerializedName("number") override val number: Int
//) : UsefulActivity

import com.example.m16_architecture.entity.FactsAboutNumbers
import com.squareup.moshi.Json

data class UsefulActivityDto(
    @Json(name = "text") override val text: String,
    @Json(name = "number") override val number: Int,
    @Json(name = "found") override val found: Boolean,
    @Json(name = "type") override val type: String
) : FactsAboutNumbers
