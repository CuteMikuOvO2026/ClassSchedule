package com.example.classschedule.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.classschedule.domain.model.Settings
import com.example.classschedule.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/**
 * Persists lightweight preferences via Jetpack DataStore Preferences.
 */
class SettingsDataStore(private val context: Context) {

    val settings: Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            currentSemesterId = prefs[KEY_CURRENT_SEMESTER_ID] ?: 1L,
            currentWeek = prefs[KEY_CURRENT_WEEK] ?: 1,
            themeMode = runCatching { ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: "") }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: false
        )
    }

    suspend fun setCurrentWeek(week: Int) {
        context.settingsDataStore.edit { it[KEY_CURRENT_WEEK] = week }
    }

    suspend fun setCurrentSemesterId(id: Long) {
        context.settingsDataStore.edit { it[KEY_CURRENT_SEMESTER_ID] = id }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    private companion object {
        val KEY_CURRENT_SEMESTER_ID = longPreferencesKey("current_semester_id")
        val KEY_CURRENT_WEEK = intPreferencesKey("current_week")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }
}
