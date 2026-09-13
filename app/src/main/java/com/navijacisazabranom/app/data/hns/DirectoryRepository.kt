package com.navijacisazabranom.app.data.hns

interface DirectoryRepository {
    /** Savezi koji imaju objavljena natjecanja u danoj sezoni. */
    suspend fun getOrganizacije(sezona: String = HnsConfig.tekucaSezona()): Result<List<Organizacija>>

    suspend fun getNatjecanja(
        organizacijaId: String,
        sezona: String = HnsConfig.tekucaSezona(),
    ): Result<List<Natjecanje>>

    /**
     * Seniorska natjecanja u kojima klub igra u danoj sezoni — liga, Hrvatski kup i
     * kup saveza. Za razliku od imenika po savezu, ovdje se vide i kupovi koje savez
     * objavi usred sezone, i to čim klub u njima dobije utakmicu.
     */
    suspend fun getNatjecanjaKluba(
        klubId: String,
        sezona: String = HnsConfig.tekucaSezona(),
    ): Result<List<Natjecanje>>
}
