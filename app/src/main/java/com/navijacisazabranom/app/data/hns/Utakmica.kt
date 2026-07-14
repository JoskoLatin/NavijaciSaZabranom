package com.navijacisazabranom.app.data.hns

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
    /** Naziv natjecanja (npr. "Liga nacija") — popunjeno za reprezentaciju, null za klupski raspored. */
    val natjecanje: String? = null,
)

/** Utakmica je odigrana ako ima rezultat ili joj je datum u prošlosti. */
fun Utakmica.jeOdigrana(danas: LocalDate = LocalDate.now()): Boolean =
    (rezultatDomacin != null && rezultatGost != null) || datum.isBefore(danas)
