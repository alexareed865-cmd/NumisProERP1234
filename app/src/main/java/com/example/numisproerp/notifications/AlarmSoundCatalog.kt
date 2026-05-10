package com.numisproerp.notifications

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Каталог стандартних звуків для нагадувань. Використовує системні рингтони.
 *
 * Власні звуки користувач може додати з будь-якого аудіо-файлу через
 * SAF — [importCustomSound] копіює його в `cacheDir/custom_sounds/` і повертає
 * `file://`-URI, який можна використовувати у налаштуваннях.
 */
data class AlarmSound(val title: String, val uri: String)

object AlarmSoundCatalog {

    /**
     * Стандартні системні звуки: Default, Alarm, Notification, Ringtone.
     * Нічого не повертатиме рінгтонів немає (рідкісний випадок).
     */
    fun systemSounds(context: Context): List<AlarmSound> {
        val list = mutableListOf<AlarmSound>()
        // 1) Default-сповіщення.
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)?.let { uri ->
            list += AlarmSound("Системний", uri.toString())
        }
        // 2) Default-будильник.
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?.let { uri ->
            list += AlarmSound("Системний будильник", uri.toString())
        }
        // 3) Default-рингтон.
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)?.let { uri ->
            list += AlarmSound("Системний рингтон", uri.toString())
        }
        // 4) Перші 5 рингтонів сповіщень з системного каталогу.
        try {
            val rm = RingtoneManager(context)
            rm.setType(RingtoneManager.TYPE_NOTIFICATION)
            val cursor = rm.cursor
            var i = 0
            while (cursor.moveToNext() && i < 5) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = rm.getRingtoneUri(cursor.position)
                if (uri != null && title != null) {
                    val asString = uri.toString()
                    if (list.none { it.uri == asString }) {
                        list += AlarmSound(title, asString)
                    }
                }
                i++
            }
        } catch (_: Exception) {
            // Деякі ROM не дають доступу до RingtoneManager.cursor — мовчки ігноруємо.
        }
        return list
    }

    /**
     * Копіює обраний користувачем аудіо-файл (Uri з SAF) у приватний каталог
     * додатка та повертає `file://`-URI або null у випадку помилки.
     */
    fun importCustomSound(context: Context, source: Uri, displayName: String): AlarmSound? {
        return try {
            val dir = File(context.cacheDir, "custom_sounds")
            if (!dir.exists()) dir.mkdirs()
            val safeName = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")
                .ifBlank { "sound_${System.currentTimeMillis()}" }
            val target = File(dir, safeName)
            context.contentResolver.openInputStream(source).use { input ->
                if (input == null) return null
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                }
            }
            AlarmSound(
                title = displayName.ifBlank { target.name },
                uri = Uri.fromFile(target).toString()
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Перелік раніше імпортованих файлів у `cacheDir/custom_sounds/`.
     */
    fun customSounds(context: Context): List<AlarmSound> {
        val dir = File(context.cacheDir, "custom_sounds")
        if (!dir.exists()) return emptyList()
        return dir.listFiles().orEmpty()
            .filter { it.isFile }
            .map { AlarmSound(title = it.name, uri = Uri.fromFile(it).toString()) }
            .sortedBy { it.title.lowercase() }
    }
}
