package com.navijacisazabranom.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navijacisazabranom.app.data.auth.trebaVerifikacijuEmaila
import com.navijacisazabranom.app.ui.screens.klub.KlubScreen
import com.navijacisazabranom.app.ui.screens.login.LoginScreen
import com.navijacisazabranom.app.ui.screens.potvrdiemail.PotvrdiEmailScreen
import com.navijacisazabranom.app.ui.screens.rang.RangScreen
import com.navijacisazabranom.app.ui.screens.raspored.RasporedScreen
import com.navijacisazabranom.app.ui.screens.registracija.RegistracijaScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val korisnik = Firebase.auth.currentUser
    val startDestination = when {
        korisnik == null -> Screen.Login.route
        korisnik.trebaVerifikacijuEmaila() -> Screen.PotvrdiEmail.route
        else -> Screen.Rang.route
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Screen.Rang.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onTrebaVerifikacija = {
                    navController.navigate(Screen.PotvrdiEmail.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegistracija = { navController.navigate(Screen.Registracija.route) },
            )
        }
        composable(Screen.Registracija.route) {
            RegistracijaScreen(
                onRegistriran = {
                    navController.navigate(Screen.PotvrdiEmail.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNatrag = { navController.popBackStack() },
            )
        }
        composable(Screen.PotvrdiEmail.route) {
            PotvrdiEmailScreen(
                onVerificiran = {
                    navController.navigate(Screen.Rang.route) {
                        popUpTo(Screen.PotvrdiEmail.route) { inclusive = true }
                    }
                },
                onOdjava = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Rang.route) {
            RangScreen(
                onNatjecanjeSelected = { natjecanjeId ->
                    navController.navigate(Screen.Klub.createRoute(natjecanjeId))
                },
            )
        }
        composable(Screen.Klub.route) { backStackEntry ->
            val natjecanjeId = backStackEntry.arguments?.getString(Screen.Klub.ARG_NATJECANJE_ID).orEmpty()
            KlubScreen(
                onKlubSelected = { klubId ->
                    navController.navigate(Screen.Raspored.createRoute(natjecanjeId, klubId))
                },
            )
        }
        composable(Screen.Raspored.route) {
            RasporedScreen()
        }
    }
}
