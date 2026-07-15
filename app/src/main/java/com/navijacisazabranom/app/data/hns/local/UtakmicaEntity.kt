package com.navijacisazabranom.app.data.hns.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "utakmica")
data class UtakmicaEntity(
    @PrimaryKey val id: String,
    val natjecanjeId: String,
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
    /** Naziv natjecanja za europske utakmice (npr. "UEFA Konferencijska liga"); null za domaći raspored. */
    val natjecanje: String? = null,
    /** Izvor podataka: "hns" (domaći raspored) ili "uefa" (europske utakmice) — zamjenjuju se neovisno. */
    val izvor: String = "hns",
)
