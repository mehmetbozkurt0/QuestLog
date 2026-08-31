package com.mehmetbozkurt.questlog.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreference {
    SYSTEM, LIGHT, DARK;

    companion object {
        val Default = DARK
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val nightModeCache: NightModeCache,
) {
    private val themeKey = stringPreferencesKey("theme_preference")
    private val paletteKey = stringPreferencesKey("palette_preference")
    private val lastSeenMessageKey = longPreferencesKey("crew_message_last_seen")

    suspend fun lastSeenCrewMessageMillis(): Long =
        dataStore.data.first()[lastSeenMessageKey] ?: 0L

    fun observeLastSeenCrewMessageMillis(): Flow<Long> =
        dataStore.data.map { prefs -> prefs[lastSeenMessageKey] ?: 0L }

    suspend fun setLastSeenCrewMessageMillis(millis: Long) {
        dataStore.edit { prefs ->
            val current = prefs[lastSeenMessageKey] ?: 0L
            if (millis > current) prefs[lastSeenMessageKey] = millis
        }
    }

    fun observePalette(): Flow<AppPalette> =
        dataStore.data.map { prefs ->
            prefs[paletteKey]?.let { value ->
                runCatching { AppPalette.valueOf(value) }.getOrNull()
            } ?: AppPalette.Default
        }

    suspend fun setPalette(palette: AppPalette) {
        dataStore.edit { prefs -> prefs[paletteKey] = palette.name }
    }

    fun observeTheme(): Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            prefs[themeKey]?.let { value ->
                runCatching { ThemePreference.valueOf(value) }.getOrNull()
            } ?: ThemePreference.Default
        }

    suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { prefs -> prefs[themeKey] = theme.name }
        nightModeCache.store(theme)
    }
}
