package com.example.m13_new_list.models

/**
 * Источник снимков. Старый api.nasa.gov/mars-photos отключён вместе с Heroku-хостингом,
 * поэтому вместо него доступны два разных API — с разной логикой запроса.
 */
enum class PhotoSource {

    /** Сырые кадры JPL: выборка по солу, снимков много, обработки нет. */
    RAW,

    /** Медиабиблиотека NASA: поиск по тексту, снимки обработанные и с описанием. */
    PROCESSED;

    /** Запрос по умолчанию, он же подсказка в поле ввода. */
    val defaultRequest: String
        get() = when (this) {
            RAW -> "1000"
            PROCESSED -> "curiosity rover"
        }
}
