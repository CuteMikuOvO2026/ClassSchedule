package com.example.classschedule.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.classschedule.ui.navigation.AppNavHost
import com.example.classschedule.ui.navigation.Routes

private data class BottomNavItem(val route: String, val label: String, val icon: String)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.TIMETABLE, "课表", "📅"),
    BottomNavItem(Routes.SETTINGS, "设置", "⚙️")
)

/**
 * Top-level app scaffold. Bottom nav has the two main destinations (课表 / 设置);
 * a floating "+" action opens the add-course screen and is shown only on the
 * timetable tab. Icons are text glyphs in this milestone (no extra dependency).
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute == Routes.TIMETABLE || currentRoute == Routes.SETTINGS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.icon) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.TIMETABLE) {
                FloatingActionButton(onClick = { navController.navigate(Routes.edit(0)) }) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
