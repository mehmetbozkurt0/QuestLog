package com.mehmetbozkurt.questlog.core.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Raw(val value: String) : UiText
}

fun uiText(@StringRes id: Int, vararg args: Any): UiText.Res = UiText.Res(id, args.toList())

fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) {
        context.getString(id)
    } else {
        val resolved = args.map { arg ->
            if (arg is UiText) arg.resolve(context) else arg
        }
        context.getString(id, *resolved.toTypedArray())
    }

    is UiText.Raw -> value
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
