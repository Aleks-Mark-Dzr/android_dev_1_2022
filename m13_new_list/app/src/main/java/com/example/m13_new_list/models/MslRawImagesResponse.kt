package com.example.m13_new_list.models

import com.google.gson.annotations.SerializedName

/** Ответ JPL Raw Images API (https://mars.nasa.gov/api/v1/raw_image_items/). */
data class MslRawImagesResponse(
    @SerializedName("items") val items: List<MslRawImage> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 0,
    @SerializedName("per_page") val perPage: Int = 0,
    @SerializedName("more") val more: Boolean = false
)

data class MslRawImage(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("sol") val sol: Int = 0,
    @SerializedName("instrument") val instrument: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image_credit") val credit: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("https_url") val httpsUrl: String? = null,
    @SerializedName("date_taken") val dateTaken: String? = null,
    @SerializedName("mission") val mission: String? = null
)

/** Кадр без ссылки на изображение показывать нечем — такие отсеиваем. */
fun MslRawImage.toPhoto(): Photo? {
    val imageUrl = imageUrl().ifBlank { return null }

    return Photo(
        id = id.toString(),
        title = title.orEmpty(),
        description = description.orEmpty(),
        credit = credit.orEmpty(),
        rover = roverName(),
        sol = sol.toString(),
        camera = cameraName(),
        date = dateTaken?.take(10).orEmpty(),
        // Превью у сырых кадров нет — в списке и в деталях одна и та же картинка
        previewUrl = imageUrl,
        fullUrl = imageUrl
    )
}

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
private fun MslRawImage.cameraName(): String =
    title?.substringAfter(':', "")?.trim()?.takeIf { it.isNotEmpty() }
        ?: instrument.orEmpty()
