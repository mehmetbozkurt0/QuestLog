package com.mehmetbozkurt.questlog.core.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mehmetbozkurt.questlog.MainActivity
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.common.startOfTodayMillis
import com.mehmetbozkurt.questlog.core.database.dao.CharacterDao
import com.mehmetbozkurt.questlog.domain.model.FeatId
import com.mehmetbozkurt.questlog.domain.progression.StreakEngine
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var characterDao: CharacterDao
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.ensureCreated(context)

        when (intent.action) {
            ACTION_QUEST_REMINDER -> {
                val logId = intent.getStringExtra(EXTRA_LOG_ID) ?: return
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
                context.notify(
                    id = logId.hashCode(),
                    channel = NotificationChannels.REMINDERS,
                    title = "Görev seni bekliyor",
                    body = title,
                    logId = logId,
                )
            }

            ACTION_STREAK_CHECK -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        checkStreak(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Seri kontrolü başarısız", e)
                    } finally {
                        scheduler.scheduleStreakCheck()
                        pending.finish()
                    }
                }
            }
        }
    }

    private suspend fun checkStreak(context: Context) {
        val uid = authRepository.currentUserSync()?.uid ?: return
        if (characterDao.ledgerCountSince(uid, startOfTodayMillis()) > 0) return

        val zone = ZoneId.systemDefault()
        val activeDays = characterDao.getLedgerTimes(uid)
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .toSet()
        val hasResolute = characterDao.getFeats(uid).any { it.featId == FeatId.RESOLUTE.name }
        val streak = StreakEngine.calculate(activeDays, LocalDate.now(zone), hasResolute)

        if (streak.currentStreak <= 0) return

        context.notify(
            id = STREAK_NOTIFICATION_ID,
            channel = NotificationChannels.STREAK,
            title = "${streak.currentStreak} günlük serin tehlikede",
            body = if (hasResolute && !streak.graceUsed) {
                "Bugün henüz görev tamamlamadın. Kararlı seni bir kez affeder, ama riske atma."
            } else {
                "Bugün henüz görev tamamlamadın. Küçük bir görev bile seriyi ayakta tutar."
            },
            logId = null,
        )
    }

    private fun Context.notify(
        id: Int,
        channel: String,
        title: String,
        body: String,
        logId: String?,
    ) {
        val manager = NotificationManagerCompat.from(this)
        if (!manager.areNotificationsEnabled()) return

        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (logId != null) putExtra(EXTRA_LOG_ID, logId)
        }
        val pending = PendingIntent.getActivity(
            this,
            id,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(this, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            manager.notify(id, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Bildirim gönderilemedi", e)
        }
    }

    companion object {
        const val ACTION_QUEST_REMINDER = "com.mehmetbozkurt.questlog.QUEST_REMINDER"
        const val ACTION_STREAK_CHECK = "com.mehmetbozkurt.questlog.STREAK_CHECK"
        const val EXTRA_LOG_ID = "logId"
        const val EXTRA_TITLE = "title"
        private const val STREAK_NOTIFICATION_ID = 90001
        private const val TAG = "Renown"
    }
}
