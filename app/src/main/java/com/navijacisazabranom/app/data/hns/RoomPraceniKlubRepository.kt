package com.navijacisazabranom.app.data.hns

import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import com.navijacisazabranom.app.data.hns.local.PraceniKlubEntity
import com.navijacisazabranom.app.data.hns.local.UtakmicaEntity
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import com.navijacisazabranom.app.notifikacije.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class RoomPraceniKlubRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val natjecanjeRepository: NatjecanjeRepository,
    private val alarmScheduler: AlarmScheduler,
    private val postavkeRepository: PostavkeRepository,
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
            zakaziNotifikacijeAkoPraceni(natjecanjeId)
        }

    override suspend fun ponovnoZakaziNotifikacije() {
        val praceniKlub = database.praceniKlubDao().get() ?: return
        zakaziNotifikacijeAkoPraceni(praceniKlub.natjecanjeId)
    }

    private suspend fun zakaziNotifikacijeAkoPraceni(natjecanjeId: String) {
        val praceniKlub = database.praceniKlubDao().get() ?: return
        if (praceniKlub.natjecanjeId != natjecanjeId) return

        val danas = LocalDate.now()
        val sljedeca = database.utakmicaDao().getZaKlub(natjecanjeId, praceniKlub.klubId)
            .filter { it.datum >= danas }
            .minByOrNull { it.datum }
            ?.toDomain()

        alarmScheduler.zakaziZaSljedecuUtakmicu(
            klubNaziv = praceniKlub.klubNaziv,
            sljedeca = sljedeca,
            vecernjiPodsjetnik = postavkeRepository.getVecernjiPodsjetnik(),
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
