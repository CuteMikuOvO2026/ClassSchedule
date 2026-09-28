package com.example.classschedule.domain.model

/**
 * User preference toggles, persisted via DataStore.
 *
 * The browsed teaching week is deliberately *not* persisted: the timetable always
 * opens on the week that contains today's date instead of restoring the last week
 * the user happened to be looking at.
 *
 * @param currentSemesterId the semester the user is currently viewing.
 * @param themeMode light / dark / follow-system.
 * @param dynamicColor whether to use Android 12+ dynamic colour (off by default so
 *   the curated course palette stays stable).
 */
data class Settings(
    val currentSemesterId: Long = 1,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
