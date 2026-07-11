package com.navijacisazabranom.app.data.hns

import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import com.navijacisazabranom.app.data.hns.local.PraceniKlubEntity
import com.navijacisazabranom.app.data.hns.local.UtakmicaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomPraceniKlubRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val natjecanjeRepository: NatjecanjeRepository,
) : PraceniKlubRepository {

    override fun observePraceniKlub(): Flow<PraceniKlub?> =
        database.praceniKlubDao().observe().map { it?.toDomain() }

    override suspend fun getPraceniKlub(): PraceniKlub? =
        database.praceniKlubDao().get()?.toDomain()

    override suspend fun postaviPraceniKlub(natjecanjeId: String, klubId: String, klubNaziv: String) {
        database.praceniKlubDao().postavi(
            PraceniKlubEntity(natjecanjeId = natjecanjeId, klubId = klubId, klubNaziv = klubNaziv),
        )
    }

    override fun observeUtakmice(natjecanjeId: String, klubId: String): Flow<List<Utakmica>> =
        database.utakmicaDao().observeZaKlub(natjecanjeId, klubId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun osvjeziUtakmice(natjecanjeId: String): Result<Unit> =
        natjecanjeRepository.getNatjecanjeStranica(natjecanjeId).map { stranica ->
            database.utakmicaDao().zamijeniZaNatjecanje(
                natjecanjeId,
                stranica.utakmice.map { it.toEntity(natjecanjeId) },
            )
        }

    private fun PraceniKlubEntity.toDomain() = PraceniKlub(natjecanjeId, klubId, klubNaziv)

    private fun UtakmicaEntity.toDomain() = Utakmica(
        id = id,
        kolo = kolo,
        datum = datum,
        vrijeme = vrijeme,
        domacinId = domacinId,
        domacinNaziv = domacinNaziv,
        gostId = gostId,
        gostNaziv = gostNaziv,
        stadion = stadion,
        rezultatDomacin = rezultatDomacin,
        rezultatGost = rezultatGost,
    )

    private fun Utakmica.toEntity(natjecanjeId: String) = UtakmicaEntity(
        id = id,
        natjecanjeId = natjecanjeId,
        kolo = kolo,
        datum = datum,
        vrijeme = vrijeme,
        domacinId = domacinId,
        domacinNaziv = domacinNaziv,
        gostId = gostId,
        gostNaziv = gostNaziv,
        stadion = stadion,
        rezultatDomacin = rezultatDomacin,
        rezultatGost = rezultatGost,
    )
}
