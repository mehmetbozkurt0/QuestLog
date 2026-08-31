package com.mehmetbozkurt.questlog.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager? get() = context.getSystemService()

    fun scheduleQuestReminder(logId: String, title: String, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_QUEST_REMINDER
            putExtra(ReminderReceiver.EXTRA_LOG_ID, logId)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
        }
        setAlarm(logId.requestCode(), intent, triggerAtMillis)
    }

    fun cancelQuestReminder(logId: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_QUEST_REMINDER
        }
        val pending = PendingIntent.getBroadcast(
            context,
            logId.requestCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pending != null) {
            alarmManager?.cancel(pending)
            pending.cancel()
        }
    }

    fun scheduleStreakCheck() {
        val zone = ZoneId.systemDefault()
        var next = ZonedDateTime.now(zone).with(LocalTime.of(STREAK_HOUR, 0))
        if (next.toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            next = next.plusDays(1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_STREAK_CHECK
        }
        setAlarm(STREAK_REQUEST_CODE, intent, next.toInstant().toEpochMilli())
    }

    private fun setAlarm(requestCode: Int, intent: Intent, triggerAtMillis: Long) {
        val manager = alarmManager ?: return
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        try {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } catch (e: SecurityException) {
            Log.e(TAG, "Alarm kurulamadı", e)
        }
    }

    private fun String.requestCode(): Int = hashCode() and 0x7FFFFFFF

    companion object {
        const val STREAK_HOUR = 20
        private const val STREAK_REQUEST_CODE = 1
        private const val TAG = "Renown"
    }
}
