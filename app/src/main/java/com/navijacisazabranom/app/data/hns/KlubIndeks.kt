package com.navijacisazabranom.app.data.hns

/** Klub u pretraživom indeksu, s ligom koja se prikazuje kao njegov rang. */
data class KlubIndeks(
    val klubId: String,
    val naziv: String,
    val grbUrl: String?,
    val natjecanjeId: String,
    val natjecanjeNaziv: String,
)
