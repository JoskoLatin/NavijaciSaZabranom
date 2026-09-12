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
import java.time.LocalTime
import javax.inject.Inject

class RoomPraceniKlubRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val natjecanjeRepository: NatjecanjeRepository,
    private val europskiRepository: EuropskiRepository,
    private val kupRepository: KupRepository,
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
            val praceniKlub = database.praceniKlubDao().get()?.takeIf { it.natjecanjeId == natjecanjeId }
            // Stanje prije osvježavanja — s njim se uspoređuju novi i promijenjeni termini.
            val stare = praceniKlub?.let { database.utakmicaDao().getZaKlub(natjecanjeId, it.klubId) }.orEmpty()

            val nove = stranica.utakmice.map { it.toEntity(natjecanjeId, IZVOR_HNS) }
            database.utakmicaDao().zamijeniZaNatjecanje(natjecanjeId, nove, IZVOR_HNS)
            osvjeziEuropske(natjecanjeId)
            if (praceniKlub != null) {
                // Nakon promjene kluba njegove već izvučene utakmice kupa nisu "nove".
                // Bez zapisa (prvi dohvat kupova nakon ažuriranja aplikacije) vrijede kao nove.
                val kupoviPoznati = postavkeRepository.getKupoviKlubId()
                    ?.let { it == praceniKlub.klubId } ?: true
                val kupovi = osvjeziKupove(praceniKlub)
                if (kupovi.sviDohvaceni) postavkeRepository.postaviKupoviKlubId(praceniKlub.klubId)
                prijaviPromjeneTermina(stare, nove, kupovi.utakmice, javiNoveUKupu = kupoviPoznati)
            }
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
                    europske.map { it.toEntity(natjecanjeId, IZVOR_UEFA) },
                    IZVOR_UEFA,
                )
            }
            .onFailure { Log.w(TAG, "Dohvat europskih utakmica nije uspio", it) }
    }

    /**
     * Utakmice kluba u kupovima (Hrvatski kup i kupovi saveza) spremaju se uz domaći
     * raspored, svaki kup kao zaseban izvor ("kup:<id>"), pa neuspjeh jednog kupa ne
     * briše utakmice ostalih. Neuspjeh se samo logira, kao kod europskih. Vraća
     * svježe dohvaćene utakmice, za usporedbu sa stanjem prije osvježavanja.
     */
    private suspend fun osvjeziKupove(praceniKlub: PraceniKlubEntity): OsvjezeniKupovi {
        val nove = mutableListOf<UtakmicaEntity>()
        var sviDohvaceni = true
        for (kup in kupRepository.kupoviZaKlub(praceniKlub.klubId)) {
            val izvor = IZVOR_KUP + kup.id
            kupRepository.getUtakmiceKluba(kup, praceniKlub.klubId)
                .onSuccess { utakmice ->
                    val entiteti = utakmice.map { it.toEntity(praceniKlub.natjecanjeId, izvor) }
                    database.utakmicaDao().zamijeniZaNatjecanje(praceniKlub.natjecanjeId, entiteti, izvor)
                    nove += entiteti
                }
                .onFailure {
                    sviDohvaceni = false
                    Log.w(TAG, "Dohvat kupa ${kup.naziv} nije uspio", it)
                }
        }
        return OsvjezeniKupovi(nove, sviDohvaceni)
    }

    /**
     * Jedna obavijest za sve: pomaknute termine prvenstva i kupa te novoizvučene
     * utakmice kupa (europske se ne uspoređuju). Prazan cache znači prvi dohvat nakon
     * odabira kluba — tada bi sve izgledalo novo, pa se ne javlja ništa.
     */
    private fun prijaviPromjeneTermina(
        stare: List<UtakmicaEntity>,
        noveDomace: List<UtakmicaEntity>,
        noveKupova: List<UtakmicaEntity>,
        javiNoveUKupu: Boolean,
    ) {
        if (stare.isEmpty()) return

        val danas = LocalDate.now()
        val stareKupova = stare.filter { it.izvor.startsWith(IZVOR_KUP) }
        val promjene = pronadjiPromjeneTermina(stare.filter { it.izvor == IZVOR_HNS }, noveDomace, danas) +
            pronadjiPromjeneTermina(stareKupova, noveKupova, danas) +
            (if (javiNoveUKupu) pronadjiNoveUtakmice(stareKupova, noveKupova, danas) else emptyList())
        notifikacijaHelper.prikaziPromjeneTermina(promjene.map { it.toDomain() })
    }

    private class OsvjezeniKupovi(
        val utakmice: List<UtakmicaEntity>,
        /** Svi kupovi kluba dohvaćeni bez greške — tek tada su njegove utakmice kupa u cacheu potpune. */
        val sviDohvaceni: Boolean,
    )

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
        prikaziPropustenDanUtakmice(praceniKlub.klubNaziv, sljedeca)
    }

    /**
     * Ako je sljedeća utakmica danas a jutarnji podsjetnik (9:00) je prošao (npr.
     * uređaj je spavao pa alarm nije zakazan), pokaži obavijest odmah pri otvaranju
     * aplikacije — jednom po utakmici (pamti se zadnja notificirana).
     */
    private suspend fun prikaziPropustenDanUtakmice(klubNaziv: String, sljedeca: Utakmica?) {
        if (sljedeca == null || sljedeca.datum != LocalDate.now()) return
        if (LocalTime.now().hour < JUTARNJI_SAT) return
        if (postavkeRepository.getZadnjaNotificiranaUtakmica() == sljedeca.id) return

        notifikacijaHelper.prikaziDanUtakmice(klubNaziv, sljedeca)
        postavkeRepository.postaviZadnjaNotificiranaUtakmica(sljedeca.id)
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
        const val JUTARNJI_SAT = 9

        const val IZVOR_HNS = "hns"
        const val IZVOR_UEFA = "uefa"

        /** Prefiks izvora za kupove; iza njega dolazi id kupa. */
        const val IZVOR_KUP = "kup:"
    }
}
