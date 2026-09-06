package com.example.classschedule.ui

import android.graphics.Color
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Adapts the system bars to the applied theme (edge-to-edge):
 * - makes the status and navigation bars transparent so the app background shows
 *   through;
 * - flips the status/navigation bar icon appearance (dark/light) to match
 *   [darkTheme] so the icons always stay readable.
 *
 * Call once near the root of the composition with the resolved dark-theme flag.
 */
@Composable
fun SystemBarStyler(darkTheme: Boolean) {
    val view = LocalView.current
    val activity = LocalActivity.current
    SideEffect {
        activity?.let { act ->
            val window = act.window
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
}
