package com.navijacisazabranom.app.data.hns.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Jedan klub u pretraživom indeksu svih seniorskih klubova. */
@Entity(
    tableName = "klub_indeks",
    indices = [Index("nazivNorm")],
)
data class KlubIndeksEntity(
    @PrimaryKey val klubId: String,
    val naziv: String,
    /** Normalizirano ime (mala slova, bez dijakritika) za LIKE pretragu. */
    @ColumnInfo(name = "nazivNorm") val nazivNorm: String,
    val grbUrl: String?,
    val natjecanjeId: String,
    val natjecanjeNaziv: String,
    val savezId: String,
    val sezona: String,
)
