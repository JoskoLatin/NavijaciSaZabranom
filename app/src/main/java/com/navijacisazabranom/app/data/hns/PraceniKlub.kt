package com.navijacisazabranom.app.data.hns

data class PraceniKlub(
    val natjecanjeId: String,
    val klubId: String,
    val klubNaziv: String,
    val grbUrl: String? = null,
    val natjecanjeNaziv: String = "",
)
