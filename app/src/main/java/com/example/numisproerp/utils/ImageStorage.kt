package com.numisproerp.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Допоміжний об'єкт для збереження вибраного користувачем фото у внутрішнє
 * сховище додатка (`filesDir/photos`). Повертає absolute path, який можна
 * передавати у Coil (AsyncImage) — він живе так само довго, як і додаток.
 *
 * Чому копіюємо: URI з MediaStore може стати недоступним після перезавантаження
 * або зміни прав; локальна копія уникає цієї проблеми.
 */
object ImageStorage {

    private const val PHOTOS_DIR = "photos"

    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
            val ext = guessExtension(context, uri) ?: "jpg"
            val destFile = File(photosDir, "${UUID.randomUUID()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun guessExtension(context: Context, uri: Uri): String? {
        val type = context.contentResolver.getType(uri) ?: return null
        return when {
            type.contains("png") -> "png"
            type.contains("webp") -> "webp"
            type.contains("gif") -> "gif"
            type.contains("jpeg") || type.contains("jpg") -> "jpg"
            else -> "jpg"
        }
    }
}
