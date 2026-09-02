package com.mehmetbozkurt.questlog.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

class AppHaptics(private val feedback: HapticFeedback) {

    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

    fun toggleOn() = feedback.performHapticFeedback(HapticFeedbackType.ToggleOn)

    fun toggleOff() = feedback.performHapticFeedback(HapticFeedbackType.ToggleOff)

    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)

    fun reject() = feedback.performHapticFeedback(HapticFeedbackType.Reject)

    fun longPress() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { AppHaptics(feedback) }
}
