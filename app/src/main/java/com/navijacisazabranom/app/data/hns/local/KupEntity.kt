package com.navijacisazabranom.app.data.hns.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Seniorski kup tekuće sezone — Hrvatski nogometni kup (savez HNS) ili kup saveza. */
@Entity(tableName = "kup")
data class KupEntity(
    @PrimaryKey val natjecanjeId: String,
    val naziv: String,
    val savezId: String,
    val sezona: String,
    /** Zadnji dohvat popisa sudionika; 0 = kup je poznat, ali sudionici još nisu dohvaćeni. */
    val azurirano: Long,
)
