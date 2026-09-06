package com.example.classschedule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.classschedule.ClassScheduleApp
import com.example.classschedule.di.AppContainer

/**
 * Convenience accessor for the application-scoped [AppContainer] from composables,
 * without a DI framework. Requires the app to be a [ClassScheduleApp].
 */
@Composable
fun appContainer(): AppContainer {
    val app = LocalContext.current.applicationContext as ClassScheduleApp
    return app.container
}
