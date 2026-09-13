package com.navijacisazabranom.app.data.hns

data class Kup(
    val id: String,
    val naziv: String,
    /** Hrvatski nogometni kup; ostalo su kupovi županijskih i mjesnih saveza. */
    val jeNacionalni: Boolean,
) {
    /**
     * Oznaka utakmice u rasporedu umjesto broja kola, npr. "Hrvatski kup, 1/16 finala".
     * Nazivi kupova saveza su neujednačeni ("KUP NSZŽ 26/27", "CUP 26/27", "HNK NSKŽ"),
     * pa se svi prikazuju kao županijski kup.
     */
    fun oznaka(runda: String?): String {
        val kup = if (jeNacionalni) "Hrvatski kup" else "Županijski kup"
        return if (runda.isNullOrBlank()) kup else "$kup, $runda"
    }
}
