package com.navijacisazabranom.app.data.hns

interface KupRepository {
    /**
     * Kupovi tekuće sezone koje za klub treba provjeriti: Hrvatski kup, kupovi saveza
     * kojem pripada klupska liga i kupovi u kojima klub već ima utakmicu.
     */
    suspend fun kupoviZaKlub(klubId: String): List<Kup>

    /** Utakmice kluba u kupu; umjesto broja kola nose oznaku kupa i runde. */
    suspend fun getUtakmiceKluba(kup: Kup, klubId: String): Result<List<Utakmica>>
}
