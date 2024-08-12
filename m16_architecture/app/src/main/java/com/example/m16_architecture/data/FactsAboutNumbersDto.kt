package com.example.m16_architecture.data

import com.example.m16_architecture.entity.FactsAboutNumbers
import com.google.gson.annotations.SerializedName

data class FactsAboutNumbersDto(
    @SerializedName("text") override val text: String,
    @SerializedName("number") override val number: Int,
    @SerializedName("found") override val found: Boolean,
    @SerializedName("type") override val type: String
) : FactsAboutNumbers