package com.mehmetbozkurt.questlog

import android.app.Application
import com.mehmetbozkurt.questlog.core.firebase.FirebaseInitializer

class QuestLogApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseInitializer.configureFirestore()
    }
}