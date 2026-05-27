package br.com.grupokyly.apscoletor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.grupokyly.apscoletor.domain.usecase.CheckSessionUseCase
import br.com.grupokyly.apscoletor.presentation.login.LoginScreen
import br.com.grupokyly.apscoletor.presentation.picking.PickingScreen

object Routes {
    const val LOGIN = "login"
    const val PICKING = "picking"
    const val DEBUG = "debug"
}

@Composable
fun ApsColetorNavGraph(checkSessionUseCase: CheckSessionUseCase) {
    var startDestination by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        startDestination = if (checkSessionUseCase()) {
            Routes.PICKING
        } else {
            Routes.LOGIN
        }
    }

    if (startDestination == null) {
        return // Show splash or empty
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToPicking = {
                    navController.navigate(Routes.PICKING) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.PICKING) {
            PickingScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PICKING) { inclusive = true }
                    }
                }
            )
        }
    }
}
