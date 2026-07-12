package com.navijacisazabranom.app.data.auth

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun jeIspravanEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

const val MIN_DULJINA_LOZINKE = 6
