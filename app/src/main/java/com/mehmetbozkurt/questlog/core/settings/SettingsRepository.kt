package com.mehmetbozkurt.questlog.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreference { SYSTEM, LIGHT, DARK }

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val themeKey = stringPreferencesKey("theme_preference")

    fun observeTheme(): Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            prefs[themeKey]?.let { value ->
                runCatching { ThemePreference.valueOf(value) }.getOrNull()
            } ?: ThemePreference.SYSTEM
        }

    suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { prefs -> prefs[themeKey] = theme.name }
    }
}
