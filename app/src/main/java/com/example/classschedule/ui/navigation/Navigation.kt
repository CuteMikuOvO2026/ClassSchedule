package com.example.classschedule.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.classschedule.ui.edit.CourseEditScreen
import com.example.classschedule.ui.settings.SettingsScreen
import com.example.classschedule.ui.timetable.TimetableScreen

object Routes {
    const val TIMETABLE = "timetable"
    const val SETTINGS = "settings"

    /** Route *pattern* (with the {courseId} placeholder Navigation expects). */
    const val EDIT = "edit?courseId={courseId}"

    /** Navigation URI for add (courseId=0) or edit (any id). */
    fun edit(courseId: Long = 0) = "edit?courseId=$courseId"
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.TIMETABLE,
        modifier = modifier
    ) {
        composable(Routes.TIMETABLE) {
            TimetableScreen(
                onAddCourse = { navController.navigate(Routes.edit(0)) },
                onEditCourse = { id -> navController.navigate(Routes.edit(id)) }
            )
        }
        composable(Routes.SETTINGS) { SettingsScreen() }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("courseId") {
                type = NavType.LongType
                defaultValue = 0L
            })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L
            CourseEditScreen(courseId = courseId, onDone = { navController.popBackStack() })
        }
    }
}
