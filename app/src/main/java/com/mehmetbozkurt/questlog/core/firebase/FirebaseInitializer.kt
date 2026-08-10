package com.mehmetbozkurt.questlog.core.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings

object FirebaseInitializer {
    fun configureFirestore() {
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {  })
        }
    }
}