package com.example.m16_new_permissions.domain.model

/**
 * Итог восстановления меток из резервной копии.
 *
 * [skipped] — метки, которые в копии оказались старее уже сохранённых: их не трогаем.
 */
data class RestoreSummary(
    val added: Int,
    val updated: Int,
    val skipped: Int
)
