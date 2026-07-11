package com.navijacisazabranom.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.navijacisazabranom.app.ui.screens.klub.KlubScreen
import com.navijacisazabranom.app.ui.screens.login.LoginScreen
import com.navijacisazabranom.app.ui.screens.rang.RangScreen
import com.navijacisazabranom.app.ui.screens.raspored.RasporedScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Screen.Rang.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Rang.route) {
            RangScreen(
                onRangSelected = { rangId ->
                    navController.navigate(Screen.Klub.createRoute(rangId))
                },
            )
        }
        composable(Screen.Klub.route) { backStackEntry ->
            val rangId = backStackEntry.arguments?.getString(Screen.Klub.ARG_RANG_ID).orEmpty()
            KlubScreen(
                rangId = rangId,
                onKlubSelected = { klubId ->
                    navController.navigate(Screen.Raspored.createRoute(klubId))
                },
            )
        }
        composable(Screen.Raspored.route) { backStackEntry ->
            val klubId = backStackEntry.arguments?.getString(Screen.Raspored.ARG_KLUB_ID).orEmpty()
            RasporedScreen(klubId = klubId)
        }
    }
}
