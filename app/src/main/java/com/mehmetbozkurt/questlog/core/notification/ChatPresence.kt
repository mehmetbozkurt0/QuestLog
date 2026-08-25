package com.mehmetbozkurt.questlog.core.notification

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatPresence @Inject constructor() {
    @Volatile
    var isChatVisible: Boolean = false
        private set

    fun setVisible(visible: Boolean) {
        isChatVisible = visible
    }
}
