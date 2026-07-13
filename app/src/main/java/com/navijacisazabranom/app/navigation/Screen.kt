package com.navijacisazabranom.app.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Registracija : Screen("registracija")
    data object PotvrdiEmail : Screen("potvrdi-email")
    data object Trazilica : Screen("trazilica")

    data object Raspored : Screen("raspored/{natjecanjeId}/{klubId}") {
        const val ARG_NATJECANJE_ID = "natjecanjeId"
        const val ARG_KLUB_ID = "klubId"
        fun createRoute(natjecanjeId: String, klubId: String) = "raspored/$natjecanjeId/$klubId"
    }
}
