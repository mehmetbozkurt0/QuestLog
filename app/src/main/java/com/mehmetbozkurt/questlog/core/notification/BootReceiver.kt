package com.mehmetbozkurt.questlog.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mehmetbozkurt.questlog.core.database.dao.QuestLogDao
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var dao: QuestLogDao
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        NotificationChannels.ensureCreated(context)
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduler.scheduleStreakCheck()
                val uid = authRepository.currentUserSync()?.uid ?: return@launch
                val now = System.currentTimeMillis()
                dao.getPendingReminders(uid, now).forEach { entity ->
                    entity.remindAtMillis?.let { at ->
                        scheduler.scheduleQuestReminder(entity.id, entity.title, at)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Alarmlar yeniden kurulamadı", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "Renown"
    }
}
