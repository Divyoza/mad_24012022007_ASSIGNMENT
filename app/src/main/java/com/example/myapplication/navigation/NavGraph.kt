package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screens.ActivitiesScreen
import com.example.myapplication.ui.screens.AttendanceScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Activities : Screen("activities")
    object Attendance : Screen("attendance")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: AppViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToActivities = {
                    navController.navigate(Screen.Activities.route)
                },
                onNavigateToAttendance = {
                    navController.navigate(Screen.Attendance.route)
                }
            )
        }
        
        composable(Screen.Activities.route) {
            ActivitiesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Attendance.route) {
            AttendanceScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

