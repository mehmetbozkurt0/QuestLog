package com.mehmetbozkurt.questlog.core.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import com.mehmetbozkurt.questlog.MainActivity
import com.mehmetbozkurt.questlog.R
import com.mehmetbozkurt.questlog.core.database.entity.CrewMessageEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrewMessageNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun notifyMessages(messages: List<CrewMessageEntity>, crewName: String?) {
        if (messages.isEmpty()) return

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        NotificationChannels.ensureCreated(context)

        val self = Person.Builder()
            .setName(context.getString(R.string.notification_chat_self))
            .build()

        val style = NotificationCompat.MessagingStyle(self)
            .setGroupConversation(true)
            .setConversationTitle(crewName ?: context.getString(R.string.notification_chat_title))

        messages.sortedBy { it.sentAtMillis }
            .takeLast(MAX_LINES)
            .forEach { message ->
                style.addMessage(
                    message.text,
                    message.sentAtMillis,
                    Person.Builder().setName(message.authorName).build(),
                )
            }

        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_CREW_CHAT, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CREW_CHAT)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setStyle(style)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Sohbet bildirimi gönderilemedi", e)
        }
    }

    fun notifyIncoming(
        authorName: String,
        text: String,
        crewName: String?,
        sentAtMillis: Long,
    ) {
        if (text.isBlank()) return
        notifyMessages(
            listOf(
                CrewMessageEntity(
                    id = "fcm-$sentAtMillis",
                    crewId = "",
                    authorId = "",
                    authorName = authorName,
                    text = text,
                    sentAtMillis = sentAtMillis,
                )
            ),
            crewName,
        )
    }

    fun clear() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    companion object {
        const val EXTRA_OPEN_CREW_CHAT = "openCrewChat"
        private const val NOTIFICATION_ID = 90002
        private const val MAX_LINES = 6
        private const val TAG = "Renown"
    }
}
