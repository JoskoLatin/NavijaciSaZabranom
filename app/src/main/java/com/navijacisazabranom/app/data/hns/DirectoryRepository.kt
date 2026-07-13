package com.navijacisazabranom.app.data.hns

interface DirectoryRepository {
    /** Savezi koji imaju objavljena natjecanja u danoj sezoni. */
    suspend fun getOrganizacije(sezona: String = HnsConfig.tekucaSezona()): Result<List<Organizacija>>

    suspend fun getNatjecanja(
        organizacijaId: String,
        sezona: String = HnsConfig.tekucaSezona(),
    ): Result<List<Natjecanje>>
}
