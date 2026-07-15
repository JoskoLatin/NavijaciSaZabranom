package com.navijacisazabranom.app.data.hns.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Jedan red (id=1) jer MVP prati samo jedan klub odjednom. */
@Entity(tableName = "praceni_klub")
data class PraceniKlubEntity(
    @PrimaryKey val id: Int = 1,
    val natjecanjeId: String,
    val klubId: String,
    val klubNaziv: String,
    val grbUrl: String? = null,
    /** Naziv ranga (npr. "SuperSport HNL") — služi i za odluku o dohvatu europskih utakmica. */
    val natjecanjeNaziv: String = "",
)
