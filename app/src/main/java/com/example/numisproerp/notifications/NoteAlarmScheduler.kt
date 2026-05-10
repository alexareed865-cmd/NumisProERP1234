package com.numisproerp.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.numisproerp.data.entities.Note
import com.numisproerp.data.settings.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Інкапсулює запис/зняття будильника AlarmManager для замітки.
 *
 * Тригер шле broadcast на [NoteAlarmReceiver], який показує сповіщення з
 * обраним у налаштуваннях звуком.
 */
@Singleton
class NoteAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsManager
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun reschedule(note: Note) {
        cancel(note.noteId)
        val triggerAt = note.reminderDate ?: return
        if (note.isCompleted) return
        if (triggerAt <= System.currentTimeMillis()) return

        val intent = buildIntent(note)
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode(note.noteId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleExact(triggerAt, pi)
    }

    fun cancel(noteId: String) {
        val intent = Intent(context, NoteAlarmReceiver::class.java).apply {
            action = NoteAlarmReceiver.ACTION_NOTE_ALARM
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode(noteId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }

    private fun buildIntent(note: Note): Intent {
        return Intent(context, NoteAlarmReceiver::class.java).apply {
            action = NoteAlarmReceiver.ACTION_NOTE_ALARM
            putExtra(NoteAlarmReceiver.EXTRA_NOTE_ID, note.noteId)
            putExtra(NoteAlarmReceiver.EXTRA_NOTE_TITLE, note.title)
            putExtra(NoteAlarmReceiver.EXTRA_NOTE_TEXT, note.text)
            putExtra(NoteAlarmReceiver.EXTRA_SOUND_URI, settings.noteAlarmSoundUri)
        }
    }

    @SuppressLint("MissingPermission")
    private fun scheduleExact(triggerAt: Long, pi: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAt, pi
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAt, pi
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pi
                )
            } else {
                @Suppress("DEPRECATION")
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: SecurityException) {
            // Якщо система відмовила у точному будильнику — fallback на неточний.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun requestCode(noteId: String): Int = noteId.hashCode()
}
