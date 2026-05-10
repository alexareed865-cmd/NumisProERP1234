package com.numisproerp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.numisproerp.MainActivity
import com.numisproerp.R

/**
 * Отримує тригер AlarmManager на запланований час нагадування і
 * показує сповіщення з вибраним користувачем звуком (як будильник).
 */
class NoteAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
        val title = intent.getStringExtra(EXTRA_NOTE_TITLE).orEmpty()
        val text = intent.getStringExtra(EXTRA_NOTE_TEXT).orEmpty()
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI).orEmpty()

        val channelId = ensureChannel(context, soundUri)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title.ifBlank { "Нагадування" })
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)

        // На Android < 26 канали не використовуються — звук задається на самому
        // повідомленні. На Android 26+ звук визначається каналом.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(resolveSoundUri(soundUri))
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(noteId.hashCode(), builder.build())
    }

    private fun ensureChannel(context: Context, soundUri: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return CHANNEL_BASE_ID
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Унікальний ID каналу залежить від URI звуку — після зміни звуку
        // в налаштуваннях створюється новий канал, бо змінити звук існуючого
        // каналу на Android 8+ неможливо.
        val safeKey = soundUri.hashCode().toString().replace("-", "n")
        val channelId = "${CHANNEL_BASE_ID}_$safeKey"
        if (nm.getNotificationChannel(channelId) == null) {
            val attrs = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            val channel = NotificationChannel(
                channelId,
                "Нагадування заміток",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Будильник для заміток з нагадуваннями"
                setSound(resolveSoundUri(soundUri), attrs)
                enableVibration(true)
                enableLights(true)
            }
            nm.createNotificationChannel(channel)
        }
        return channelId
    }

    private fun resolveSoundUri(soundUri: String): Uri {
        return if (soundUri.isBlank()) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            try {
                Uri.parse(soundUri)
            } catch (_: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }
    }

    companion object {
        const val ACTION_NOTE_ALARM = "com.numisproerp.action.NOTE_ALARM"
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_NOTE_TITLE = "note_title"
        const val EXTRA_NOTE_TEXT = "note_text"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val CHANNEL_BASE_ID = "note_alarm_channel"
    }
}
