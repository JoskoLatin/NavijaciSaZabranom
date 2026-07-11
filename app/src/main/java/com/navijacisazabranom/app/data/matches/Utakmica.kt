package com.navijacisazabranom.app.data.matches

import java.time.LocalDate
import java.time.LocalTime

data class Utakmica(
    val id: String,
    val kolo: Int,
    val datum: LocalDate,
    val vrijeme: LocalTime?,
    val domacinId: String,
    val domacinNaziv: String,
    val gostId: String,
    val gostNaziv: String,
    val stadion: String?,
    val rezultatDomacin: Int?,
    val rezultatGost: Int?,
)
