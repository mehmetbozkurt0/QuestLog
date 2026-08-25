package com.mehmetbozkurt.questlog.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.mehmetbozkurt.questlog.R

object NotificationChannels {
    const val REMINDERS = "quest_reminders"
    const val STREAK = "streak_warnings"
    const val CREW_CHAT = "crew_chat"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return

        val reminders = NotificationChannel(
            REMINDERS,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
            enableVibration(true)
        }

        val streak = NotificationChannel(
            STREAK,
            context.getString(R.string.streak_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.streak_channel_description)
        }

        val crewChat = NotificationChannel(
            CREW_CHAT,
            context.getString(R.string.crew_chat_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.crew_chat_channel_description)
            enableVibration(true)
        }

        manager.createNotificationChannels(listOf(reminders, streak, crewChat))
    }
}
