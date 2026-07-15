package com.navijacisazabranom.app.data.hns

interface EuropskiRepository {
    /**
     * Europske utakmice (Liga prvaka / Europska / Konferencijska liga) praćenog
     * kluba za tekuću sezonu; prazna lista ako klub trenutačno ne igra u Europi.
     */
    suspend fun getEuropskeUtakmice(klubNaziv: String, klubId: String): Result<List<Utakmica>>
}
