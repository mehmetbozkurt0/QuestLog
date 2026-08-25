package com.mehmetbozkurt.questlog.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RenownMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokenManager: DeviceTokenManager
    @Inject lateinit var chatPresence: ChatPresence
    @Inject lateinit var notifier: CrewMessageNotifier
    @Inject lateinit var authRepository: AuthRepository
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onNewToken(token: String) {
        scope.launch { tokenManager.register(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["type"] != TYPE_CREW_MESSAGE) return
        if (chatPresence.isChatVisible) return

        val authorId = data["authorId"].orEmpty()
        if (authorId == authRepository.currentUserSync()?.uid) return

        notifier.notifyIncoming(
            authorName = data["authorName"].orEmpty(),
            text = data["text"].orEmpty(),
            crewName = data["crewName"].orEmpty().ifBlank { null },
            sentAtMillis = data["sentAtMillis"]?.toLongOrNull()
                ?: System.currentTimeMillis(),
        )
    }

    private companion object {
        const val TYPE_CREW_MESSAGE = "crew_message"
    }
}
