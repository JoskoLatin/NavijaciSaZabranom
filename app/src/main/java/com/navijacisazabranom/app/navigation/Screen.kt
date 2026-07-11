package com.navijacisazabranom.app.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Rang : Screen("rang")

    data object Klub : Screen("klub/{natjecanjeId}") {
        const val ARG_NATJECANJE_ID = "natjecanjeId"
        fun createRoute(natjecanjeId: String) = "klub/$natjecanjeId"
    }

    data object Raspored : Screen("raspored/{natjecanjeId}/{klubId}") {
        const val ARG_NATJECANJE_ID = "natjecanjeId"
        const val ARG_KLUB_ID = "klubId"
        fun createRoute(natjecanjeId: String, klubId: String) = "raspored/$natjecanjeId/$klubId"
    }
}
