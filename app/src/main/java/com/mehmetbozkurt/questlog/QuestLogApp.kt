package com.mehmetbozkurt.questlog

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mehmetbozkurt.questlog.core.common.ApplicationScope
import com.mehmetbozkurt.questlog.core.firebase.FirebaseInitializer
import com.mehmetbozkurt.questlog.core.sync.RemoteSyncManager
import com.mehmetbozkurt.questlog.domain.repository.AuthRepository
import com.mehmetbozkurt.questlog.domain.repository.CharacterRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltAndroidApp
class QuestLogApp: Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var remoteSyncManager: RemoteSyncManager
    @Inject lateinit var characterRepository: CharacterRepository
    @Inject lateinit var authRepository: AuthRepository
    @ApplicationScope @Inject lateinit var appScope: CoroutineScope

    override val workManagerConfiguration: Configuration get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseInitializer.configureFirestore()
        remoteSyncManager.start()

        authRepository.currentUser.filterNotNull().onEach { characterRepository.ensureCharacter() }.launchIn(appScope)
    }
}