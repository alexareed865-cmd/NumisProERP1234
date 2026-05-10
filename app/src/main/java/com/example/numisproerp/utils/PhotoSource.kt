package com.numisproerp.utils

import java.io.File

/**
 * Повертає об'єкт, який можна передати в [coil.request.ImageRequest.Builder.data]:
 *  - для http(s) URL — сам рядок (Coil завантажить мережею),
 *  - інакше — `File`, бо в БД зберігаємо локальні шляхи.
 */
fun photoModel(path: String): Any {
    val trimmed = path.trim()
    return if (trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
    ) {
        trimmed
    } else {
        File(trimmed)
    }
}
