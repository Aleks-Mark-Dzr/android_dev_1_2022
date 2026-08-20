package com.example.m13_new_list.models

import com.google.gson.annotations.SerializedName

/**
 * Ответ JPL Raw Images API (https://mars.nasa.gov/api/v1/raw_image_items/).
 * Пришёл на смену api.nasa.gov/mars-photos, который отключён вместе с Heroku-хостингом.
 */
data class MslRawImagesResponse(
    @SerializedName("items") val items: List<MslRawImage> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 0,
    @SerializedName("per_page") val perPage: Int = 0,
    @SerializedName("more") val more: Boolean = false
)

data class MslRawImage(
    @SerializedName("id") val id: Int,
    @SerializedName("sol") val sol: Int,
    @SerializedName("instrument") val instrument: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("https_url") val httpsUrl: String?,
    @SerializedName("date_taken") val dateTaken: String?,
    @SerializedName("mission") val mission: String?
)

/** Приводим ответ нового API к модели, с которой уже работают адаптер и экран деталей. */
fun MslRawImage.toPhoto(): Photo = Photo(
    id = id,
    sol = sol,
    img_src = imageUrl(),
    earth_date = dateTaken?.take(10).orEmpty(),
    rover = Rover(name = roverName()),
    camera = Camera(name = instrument.orEmpty(), full_name = cameraFullName())
)

/** У части снимков https_url отсутствует, а url отдаётся по http — принудительно поднимаем схему. */
private fun MslRawImage.imageUrl(): String {
    val source = httpsUrl?.takeIf { it.isNotBlank() } ?: url.orEmpty()
    return if (source.startsWith("http://")) source.replaceFirst("http://", "https://") else source
}

private fun MslRawImage.roverName(): String = when (mission?.lowercase()) {
    "msl" -> "Curiosity"
    "mars2020" -> "Perseverance"
    "mer1", "mer2" -> "Opportunity"
    else -> mission.orEmpty()
}

/** title приходит в виде "Sol 1000: Mast Camera (Mastcam)" — берём часть после двоеточия. */
private fun MslRawImage.cameraFullName(): String =
    title?.substringAfter(':', "")?.trim()?.takeIf { it.isNotEmpty() }
        ?: instrument.orEmpty()
