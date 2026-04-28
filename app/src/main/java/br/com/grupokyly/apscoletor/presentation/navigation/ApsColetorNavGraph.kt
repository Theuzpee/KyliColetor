package br.com.grupokyly.apscoletor.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.presentation.picking.PickingScreen

object Routes {
    const val LOGIN = "login"
    const val PICKING = "picking"
}

@Composable
fun ApsColetorNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { navController.navigate(Routes.PICKING) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                } }) {
                    Text(stringResource(R.string.login_mock_button))
                }
            }
        }
        
        composable(Routes.PICKING) {
            PickingScreen()
        }
    }
}
