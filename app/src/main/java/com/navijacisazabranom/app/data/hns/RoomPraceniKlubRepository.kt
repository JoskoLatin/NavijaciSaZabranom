package com.navijacisazabranom.app.data.hns

import android.util.Log
import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import com.navijacisazabranom.app.data.hns.local.PraceniKlubEntity
import com.navijacisazabranom.app.data.hns.local.UtakmicaEntity
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import com.navijacisazabranom.app.notifikacije.AlarmScheduler
import com.navijacisazabranom.app.notifikacije.NotifikacijaHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class RoomPraceniKlubRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val natjecanjeRepository: NatjecanjeRepository,
    private val europskiRepository: EuropskiRepository,
    private val alarmScheduler: AlarmScheduler,
    private val postavkeRepository: PostavkeRepository,
    private val notifikacijaHelper: NotifikacijaHelper,
) : PraceniKlubRepository {

    override fun observePraceniKlub(): Flow<PraceniKlub?> =
        database.praceniKlubDao().observe().map { it?.toDomain() }

    override suspend fun getPraceniKlub(): PraceniKlub? =
        database.praceniKlubDao().get()?.toDomain()

    override suspend fun postaviPraceniKlub(
        natjecanjeId: String,
        klubId: String,
        klubNaziv: String,
        grbUrl: String?,
        natjecanjeNaziv: String,
    ) {
        database.praceniKlubDao().postavi(
            PraceniKlubEntity(
                natjecanjeId = natjecanjeId,
                klubId = klubId,
                klubNaziv = klubNaziv,
                grbUrl = grbUrl,
                natjecanjeNaziv = natjecanjeNaziv,
            ),
        )
    }

    override fun observeUtakmice(natjecanjeId: String, klubId: String): Flow<List<Utakmica>> =
        database.utakmicaDao().observeZaKlub(natjecanjeId, klubId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun osvjeziUtakmice(natjecanjeId: String): Result<Unit> =
        natjecanjeRepository.getNatjecanjeStranica(natjecanjeId).map { stranica ->
            val nove = stranica.utakmice.map { it.toEntity(natjecanjeId, "hns") }
            prijaviPromjeneTermina(natjecanjeId, nove)
            database.utakmicaDao().zamijeniZaNatjecanje(natjecanjeId, nove, "hns")
            osvjeziEuropske(natjecanjeId)
            zakaziNotifikacijeAkoPraceni(natjecanjeId)
        }

    /**
     * Za klubove najvišeg ranga (HNL) dohvaća europske utakmice i sprema ih uz
     * domaći raspored (izvor "uefa"), pa ih klupski upit i notifikacije pokupe.
     * Neuspjeh se samo logira — europski dio nikad ne ruši domaći raspored.
     */
    private suspend fun osvjeziEuropske(natjecanjeId: String) {
        val praceniKlub = database.praceniKlubDao().get() ?: return
        if (praceniKlub.natjecanjeId != natjecanjeId) return
        if (!praceniKlub.natjecanjeNaziv.contains("HNL", ignoreCase = true)) return

        europskiRepository.getEuropskeUtakmice(praceniKlub.klubNaziv, praceniKlub.klubId)
            .onSuccess { europske ->
                database.utakmicaDao().zamijeniZaNatjecanje(
                    natjecanjeId,
                    europske.map { it.toEntity(natjecanjeId, "uefa") },
                    "uefa",
                )
            }
            .onFailure { Log.w(TAG, "Dohvat europskih utakmica nije uspio", it) }
    }

    private suspend fun prijaviPromjeneTermina(natjecanjeId: String, nove: List<UtakmicaEntity>) {
        val praceniKlub = database.praceniKlubDao().get() ?: return
        if (praceniKlub.natjecanjeId != natjecanjeId) return

        // Usporedba samo domaćeg izvora — europske se osvježavaju zasebno.
        val stare = database.utakmicaDao().getZaKlub(natjecanjeId, praceniKlub.klubId)
            .filter { it.izvor == "hns" }
        val promjene = pronadjiPromjeneTermina(stare, nove, LocalDate.now())
        notifikacijaHelper.prikaziPromjeneTermina(promjene.map { it.toDomain() })
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

    private fun PraceniKlubEntity.toDomain() =
        PraceniKlub(natjecanjeId, klubId, klubNaziv, grbUrl, natjecanjeNaziv)

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
        natjecanje = natjecanje,
    )

    private fun Utakmica.toEntity(natjecanjeId: String, izvor: String) = UtakmicaEntity(
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
        natjecanje = natjecanje,
        izvor = izvor,
    )

    private companion object {
        const val TAG = "PraceniKlubRepo"
    }
}
