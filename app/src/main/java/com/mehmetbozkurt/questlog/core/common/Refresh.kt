package com.mehmetbozkurt.questlog.core.common

import android.os.SystemClock
import kotlinx.coroutines.delay

const val MIN_REFRESH_MILLIS = 600L

suspend fun withMinimumDuration(minMillis: Long = MIN_REFRESH_MILLIS, block: suspend () -> Unit) {
    val started = SystemClock.elapsedRealtime()
    runCatching { block() }
    val elapsed = SystemClock.elapsedRealtime() - started
    if (elapsed < minMillis) delay(minMillis - elapsed)
}
