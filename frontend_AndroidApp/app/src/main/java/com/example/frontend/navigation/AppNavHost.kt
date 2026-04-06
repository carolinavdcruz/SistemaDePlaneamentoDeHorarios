package com.example.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.frontend.AppModule
import com.example.frontend.ui.screens.DashboardScreen
import com.example.frontend.ui.screens.HomeScreen
import com.example.frontend.ui.screens.LoginScreen
import com.example.frontend.ui.screens.ProfileScreen
import com.example.frontend.ui.screens.RegisterScreen

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onStartClick = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.LOGIN) {
            val viewModel = remember { AppModule.provideLoginViewModel() }
            val loginSuccess by viewModel.loginSuccess.collectAsState()

            LaunchedEffect(loginSuccess) {
                if (loginSuccess) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                viewModel    = viewModel,
                onLoginClick = { viewModel.login() },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            val viewModel = remember { AppModule.provideRegisterViewModel() }
            val registerSuccess by viewModel.registerSuccess.collectAsState()

            LaunchedEffect(registerSuccess) {
                if (registerSuccess) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                viewModel = viewModel,
                onLoginClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccountClick = { viewModel.register() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                navController = navController,
                onSignOutClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }
    }
}
