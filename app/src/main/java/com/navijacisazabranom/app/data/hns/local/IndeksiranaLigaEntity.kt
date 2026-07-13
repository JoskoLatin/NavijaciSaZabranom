package com.navijacisazabranom.app.data.hns.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Evidencija obrađenih liga — omogućuje nastavak prekinutog indeksiranja bez ponovnog dohvata. */
@Entity(tableName = "indeksirana_liga")
data class IndeksiranaLigaEntity(
    @PrimaryKey val natjecanjeId: String,
    val sezona: String,
    val azurirano: Long,
)
