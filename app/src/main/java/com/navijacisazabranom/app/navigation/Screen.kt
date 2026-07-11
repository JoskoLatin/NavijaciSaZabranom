package com.navijacisazabranom.app.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Rang : Screen("rang")

    data object Klub : Screen("klub/{rangId}") {
        const val ARG_RANG_ID = "rangId"
        fun createRoute(rangId: String) = "klub/$rangId"
    }

    data object Raspored : Screen("raspored/{klubId}") {
        const val ARG_KLUB_ID = "klubId"
        fun createRoute(klubId: String) = "raspored/$klubId"
    }
}
