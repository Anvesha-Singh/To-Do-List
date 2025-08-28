package com.example.taskmaster.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.taskmaster.screens.CalendarScreen
import com.example.taskmaster.screens.DailyWeeklyScreen
import com.example.taskmaster.screens.StatsScreen
import com.example.taskmaster.screens.TasksScreen

sealed class BottomDest(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    data object Tasks : BottomDest("tasks", "Tasks", android.R.drawable.ic_menu_agenda)
    data object DailyWeekly : BottomDest("daily_weekly", "Daily/Weekly", android.R.drawable.ic_menu_my_calendar)
    data object Calendar : BottomDest("calendar", "Calendar", android.R.drawable.ic_menu_today)
    data object Stats : BottomDest("stats", "Stats", android.R.drawable.ic_menu_view)
}

@Composable
fun NavGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(navController = navController, startDestination = BottomDest.Tasks.route) {
        composable(BottomDest.Tasks.route) {
            TasksScreen(modifier = Modifier.padding(paddingValues))
        }
        composable(BottomDest.DailyWeekly.route) {
            DailyWeeklyScreen(modifier = Modifier.padding(paddingValues))
        }
        composable(BottomDest.Calendar.route) {
            CalendarScreen(modifier = Modifier.padding(paddingValues))
        }
        composable(BottomDest.Stats.route) {
            StatsScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}
