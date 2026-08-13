package com.mehmetbozkurt.questlog

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mehmetbozkurt.questlog.core.firebase.FirebaseInitializer
import com.mehmetbozkurt.questlog.core.sync.RemoteSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QuestLogApp: Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var remoteSyncManager: RemoteSyncManager

    override val workManagerConfiguration: Configuration get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseInitializer.configureFirestore()
        remoteSyncManager.start()
    }
}