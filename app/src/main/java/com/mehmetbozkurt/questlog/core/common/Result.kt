package com.mehmetbozkurt.questlog.core.common

sealed interface DataResult<out T> {
    data class Success<T>(val data: T): DataResult<T>
    data class Error(val exception: Throwable) : DataResult<Nothing>
}

inline fun <T> runCatchingResult(block: () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Error(e)
    }