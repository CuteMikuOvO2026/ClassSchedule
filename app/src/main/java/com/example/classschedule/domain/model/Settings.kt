package com.example.classschedule.domain.model

/**
 * User preference toggles, persisted via DataStore.
 *
 * @param currentSemesterId the semester the user is currently viewing.
 * @param currentWeek the teaching week the user is viewing the timetable for.
 * @param themeMode light / dark / follow-system.
 * @param dynamicColor whether to use Android 12+ dynamic colour (off by default so
 *   the curated course palette stays stable).
 */
data class Settings(
    val currentSemesterId: Long = 1,
    val currentWeek: Int = 1,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
