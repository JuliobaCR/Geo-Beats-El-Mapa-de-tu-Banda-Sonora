package com.geobeats.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.geobeats.app.presentation.view.*
import com.geobeats.app.presentation.viewmodel.MapViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object ModeSelection : Screen("mode_selection")
    object Login : Screen("login")
    object UserMap : Screen("user_map")
    object DeveloperMap : Screen("developer_map")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MapViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNext = {
                navController.navigate(Screen.ModeSelection.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.ModeSelection.route) {
            ModeSelectionScreen(
                onUserModeSelected = { navController.navigate(Screen.UserMap.route) },
                onDevModeSelected = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DeveloperMap.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.UserMap.route) {
            UserMapScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.DeveloperMap.route) {
            DeveloperMapScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
