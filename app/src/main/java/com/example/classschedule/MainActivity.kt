package com.example.classschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.classschedule.domain.model.Settings
import com.example.classschedule.domain.model.ThemeMode
import com.example.classschedule.ui.MainScreen
import com.example.classschedule.ui.SystemBarStyler
import com.example.classschedule.ui.appContainer
import com.example.classschedule.ui.theme.ClassScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

/**
 * Collects user theme preferences from DataStore and applies them to the app
 * theme, so the "theme mode" and "dynamic colour" settings take effect.
 */
@Composable
private fun AppRoot() {
    val container = appContainer()
    val settings by container.settingsDataStore.settings
        .collectAsStateWithLifecycle(initialValue = Settings())

    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    ClassScheduleTheme(
        darkTheme = darkTheme,
        dynamicColor = settings.dynamicColor
    ) {
        // Keep the status/navigation bars transparent (edge-to-edge) and flip
        // their icon appearance so they stay readable in both light & dark themes.
        SystemBarStyler(darkTheme)
        MainScreen()
    }
}
