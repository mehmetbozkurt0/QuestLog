package com.mehmetbozkurt.questlog

import android.app.Application
import com.mehmetbozkurt.questlog.core.firebase.FirebaseInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuestLogApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseInitializer.configureFirestore()
    }
}