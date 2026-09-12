package com.navijacisazabranom.app.data.hns.local

import androidx.room.Entity

/**
 * Klub koji ima barem jednu utakmicu u kupu. Po tome sinkronizacija zna koje
 * kupove praćenog kluba treba dohvatiti, bez dohvata svih kupova u zemlji.
 */
@Entity(tableName = "kup_sudionik", primaryKeys = ["kupId", "klubId"])
data class KupSudionikEntity(
    val kupId: String,
    val klubId: String,
)
