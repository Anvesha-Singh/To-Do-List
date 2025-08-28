package com.example.taskmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskmaster.navigation.BottomDest
import com.example.taskmaster.navigation.NavGraph
import com.example.taskmaster.ui.theme.ComposeSkeletonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeSkeletonTheme {
                val navController = rememberNavController()
                val items = listOf(
                    BottomDest.Tasks,
                    BottomDest.DailyWeekly,
                    BottomDest.Calendar,
                    BottomDest.Stats
                )
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            items.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentRoute == dest.route,
                                    onClick = {
                                        if (currentRoute != dest.route) {
                                            navController.navigate(dest.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            painterResource(id = dest.iconRes),
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    NavGraph(navController = navController, paddingValues = paddingValues)
                }
            }
        }
    }
}
