package com.mehmetbozkurt.questlog.core.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NightModeCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        context.getSharedPreferences("night_mode", Context.MODE_PRIVATE)
    }

    fun apply() {
        AppCompatDelegate.setDefaultNightMode(read().toDelegateMode())
    }

    fun store(theme: ThemePreference) {
        prefs.edit().putString(KEY, theme.name).apply()
        AppCompatDelegate.setDefaultNightMode(theme.toDelegateMode())
    }

    private fun read(): ThemePreference =
        prefs.getString(KEY, null)
            ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
            ?: ThemePreference.Default

    private fun ThemePreference.toDelegateMode(): Int = when (this) {
        ThemePreference.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }

    private companion object {
        const val KEY = "theme_preference"
    }
}
