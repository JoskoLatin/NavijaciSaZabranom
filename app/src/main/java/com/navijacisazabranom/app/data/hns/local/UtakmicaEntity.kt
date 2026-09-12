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
    /**
     * Naziv natjecanja za kup i europske utakmice (npr. "Hrvatski kup, 1/16 finala",
     * "UEFA Konferencijska liga"); null za domaći raspored.
     */
    val natjecanje: String? = null,
    /**
     * Izvor podataka: "hns" (domaći raspored), "uefa" (europske utakmice) ili "kup:<id>"
     * (jedan kup) — svaki se zamjenjuje neovisno.
     */
    val izvor: String = "hns",
)
