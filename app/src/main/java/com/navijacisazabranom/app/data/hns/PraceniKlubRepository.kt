package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.flow.Flow

interface PraceniKlubRepository {
    fun observePraceniKlub(): Flow<PraceniKlub?>
    suspend fun getPraceniKlub(): PraceniKlub?
    suspend fun postaviPraceniKlub(natjecanjeId: String, klubId: String, klubNaziv: String)
    fun observeUtakmice(natjecanjeId: String, klubId: String): Flow<List<Utakmica>>

    /** Dohvaća natjecanje s mreže i zamjenjuje cache za taj natjecanjeId. */
    suspend fun osvjeziUtakmice(natjecanjeId: String): Result<Unit>
}
