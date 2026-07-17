package com.navijacisazabranom.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navijacisazabranom.app.data.auth.trebaVerifikacijuEmaila
import com.navijacisazabranom.app.ui.screens.login.LoginScreen
import com.navijacisazabranom.app.ui.screens.potvrdiemail.PotvrdiEmailScreen
import com.navijacisazabranom.app.ui.screens.profil.ProfilScreen
import com.navijacisazabranom.app.ui.screens.raspored.RasporedScreen
import com.navijacisazabranom.app.ui.screens.registracija.RegistracijaScreen
import com.navijacisazabranom.app.ui.screens.reprezentacija.ReprezentacijaScreen
import com.navijacisazabranom.app.ui.screens.trazilica.TrazilicaScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val korisnik = Firebase.auth.currentUser
    val startDestination = when {
        korisnik == null -> Screen.Login.route
        korisnik.trebaVerifikacijuEmaila() -> Screen.PotvrdiEmail.route
        else -> Screen.Profil.route
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Screen.Profil.route) {
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
                    navController.navigate(Screen.Profil.route) {
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
        composable(Screen.Profil.route) {
            ProfilScreen(
                onPromijeniKlub = { navController.navigate(Screen.Trazilica.route) },
                onOtvoriRaspored = { natjecanjeId, klubId ->
                    navController.navigate(Screen.Raspored.createRoute(natjecanjeId, klubId))
                },
                onReprezentacija = { navController.navigate(Screen.Reprezentacija.route) },
                onNemaKluba = {
                    navController.navigate(Screen.Trazilica.route) {
                        popUpTo(Screen.Profil.route) { inclusive = true }
                    }
                },
                onOdjava = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Trazilica.route) {
            TrazilicaScreen(
                onKlubOdabran = { _, _ ->
                    navController.navigate(Screen.Profil.route) {
                        popUpTo(Screen.Profil.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Reprezentacija.route) {
            ReprezentacijaScreen()
        }
        composable(Screen.Raspored.route) {
            RasporedScreen()
        }
    }
}
