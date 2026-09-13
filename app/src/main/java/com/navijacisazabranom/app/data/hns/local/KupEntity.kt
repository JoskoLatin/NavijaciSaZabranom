package com.navijacisazabranom.app.data.hns.local

import androidx.room.Entity

/**
 * Kup u kojem klub igra ove sezone. Popis dolazi sa semafora pri svakoj
 * sinkronizaciji; zapamćen je da kupovi ne ispadnu iz rasporeda ako taj upit padne.
 */
@Entity(tableName = "kup", primaryKeys = ["klubId", "natjecanjeId"])
data class KupEntity(
    val klubId: String,
    val natjecanjeId: String,
    val naziv: String,
    val sezona: String,
    val nacionalni: Boolean,
)
