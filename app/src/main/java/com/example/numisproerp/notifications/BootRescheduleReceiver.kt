package com.numisproerp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.numisproerp.di.AppDatabaseEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Після перезавантаження пристрою або оновлення додатка перепланує
 * AlarmManager-будильники для всіх заміток із майбутніми нагадуваннями.
 */
class BootRescheduleReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SchedulerEntryPoint {
        fun scheduler(): NoteAlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = EntryPointAccessors
                    .fromApplication(context.applicationContext, AppDatabaseEntryPoint::class.java)
                    .appDatabase()
                val scheduler = EntryPointAccessors
                    .fromApplication(context.applicationContext, SchedulerEntryPoint::class.java)
                    .scheduler()

                val now = System.currentTimeMillis()
                val notes = db.noteDao().getFutureReminders(now)
                notes.forEach { scheduler.reschedule(it) }
            } catch (_: Exception) {
                // best-effort
            } finally {
                pending.finish()
            }
        }
    }
}
