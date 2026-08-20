package com.example.m13_new_list.models

import com.google.gson.annotations.SerializedName

/**
 * Ответ NASA Image and Video Library (https://images-api.nasa.gov/search).
 * Пришёл на смену api.nasa.gov/mars-photos, который отключён вместе с Heroku-хостингом.
 */
data class NasaImagesResponse(
    @SerializedName("collection") val collection: ImageCollection? = null
)

data class ImageCollection(
    @SerializedName("items") val items: List<CollectionItem> = emptyList(),
    @SerializedName("metadata") val metadata: CollectionMetadata? = null
)

data class CollectionMetadata(
    @SerializedName("total_hits") val totalHits: Int = 0
)

data class CollectionItem(
    @SerializedName("data") val data: List<ImageData> = emptyList(),
    @SerializedName("links") val links: List<ImageLink> = emptyList()
)

data class ImageData(
    @SerializedName("nasa_id") val nasaId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("date_created") val dateCreated: String?,
    @SerializedName("center") val center: String?,
    @SerializedName("secondary_creator") val credit: String?,
    @SerializedName("keywords") val keywords: List<String> = emptyList()
)

data class ImageLink(
    @SerializedName("href") val href: String?,
    @SerializedName("rel") val rel: String?,
    @SerializedName("render") val render: String?
)

/** Элемент без данных или без картинки показывать нечем — такие отсеиваем. */
fun CollectionItem.toPhoto(): Photo? {
    val imageData = data.firstOrNull() ?: return null
    val preview = imageUrl(REL_PREVIEW) ?: imageUrl(REL_CANONICAL) ?: return null
    val full = imageUrl(REL_CANONICAL) ?: preview
    // Описание в выдаче заканчивается ссылкой на каталог — она в тексте не нужна
    val description = imageData.description.orEmpty().substringBefore(" https://").trim()

    return Photo(
        id = imageData.nasaId.orEmpty(),
        title = imageData.title.orEmpty(),
        description = description,
        credit = imageData.credit ?: imageData.center.orEmpty(),
        rover = detectRover(imageData, description),
        // Сол и камера в медиабиблиотеке отдельными полями не приходят — только текстом в описании
        sol = SOL_PATTERN.find(description)?.groupValues?.get(1).orEmpty(),
        camera = "",
        date = imageData.dateCreated?.take(10).orEmpty(),
        previewUrl = preview,
        fullUrl = full
    )
}

/** Ссылки на ассеты изредка приходят по http — принудительно поднимаем схему. */
private fun CollectionItem.imageUrl(rel: String): String? =
    links.firstOrNull { it.rel == rel && !it.href.isNullOrBlank() }
        ?.href
        ?.replaceFirst("http://", "https://")

/**
 * Названия марсоходов в выдаче встречаются и в заголовке, и в ключевых словах.
 * Описание проверяем в последнюю очередь: там марсоход могут упомянуть вскользь.
 */
private fun detectRover(imageData: ImageData, description: String): String {
    val primary = (imageData.keywords + listOfNotNull(imageData.title)).joinToString(" ").lowercase()
    return ROVERS.firstOrNull { primary.contains(it.lowercase()) }
        ?: ROVERS.firstOrNull { description.contains(it, ignoreCase = true) }.orEmpty()
}

private const val REL_PREVIEW = "preview"
private const val REL_CANONICAL = "canonical"
private val ROVERS = listOf("Curiosity", "Perseverance", "Opportunity", "Spirit", "Sojourner")
private val SOL_PATTERN = Regex("""[Ss]ol (\d+)""")
