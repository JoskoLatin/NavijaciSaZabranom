package com.navijacisazabranom.app.data.hns

import android.util.Log
import com.navijacisazabranom.app.data.hns.local.IndeksiranaLigaEntity
import com.navijacisazabranom.app.data.hns.local.KlubIndeksEntity
import com.navijacisazabranom.app.data.hns.local.KupEntity
import com.navijacisazabranom.app.data.hns.local.KupSudionikEntity
import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HnsKlubIndeksRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val directoryRepository: DirectoryRepository,
    private val natjecanjeRepository: NatjecanjeRepository,
    private val postavkeRepository: PostavkeRepository,
) : KlubIndeksRepository {

    private val dao get() = database.klubIndeksDao()
    private val kupDao get() = database.kupDao()

    override fun pretrazi(upit: String): Flow<List<KlubIndeks>> {
        // % i _ su LIKE wildcardi — iz korisničkog upita se uklanjaju.
        val upitNorm = normalizirajZaPretragu(upit).replace("%", "").replace("_", "")
        return dao.pretrazi(upitNorm).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeBrojKlubova(): Flow<Int> = dao.observeBrojKlubova()

    override suspend fun indeksZastario(): Boolean {
        // Promjena filtra znači da su prije indeksirane lige možda pogrešno
        // uključene — mora se ponovno izgraditi bez obzira na svježinu.
        if (postavkeRepository.getIndeksVerzija() != SeniorskiFiltar.VERZIJA) return true
        if (dao.observeBrojKlubova().first() == 0) return true
        val zadnje = dao.zadnjeIndeksiranje() ?: return true
        val sada = System.currentTimeMillis()
        if (sada - zadnje > SVJEZINA_INDEKSA_MS) return true
        // Kupovi zastarijevaju brže od liga: klubovi viših rangova ulaze tek u kasnijim rundama.
        return sada - postavkeRepository.getPregledKupova() > SVJEZINA_KUPOVA_MS
    }

    override suspend fun izgradiIndeks(napredak: (Int, Int) -> Unit): Result<Unit> = runCatching {
        // Novi filtar → stari indeks se u cijelosti odbacuje (klubovi kupova i
        // seniorskih liga se preklapaju pa selektivno brisanje po ligi nije
        // pouzdano — čist rebuild je jedini siguran put).
        if (postavkeRepository.getIndeksVerzija() != SeniorskiFiltar.VERZIJA) {
            dao.obrisiSveKlubove()
            dao.obrisiSveIndeksiraneLige()
        }

        val tekuca = HnsConfig.tekucaSezona()
        val prosla = HnsConfig.proslaSezona()

        val orgsTekuca = directoryRepository.getOrganizacije(tekuca).getOrElse { emptyList() }
        delay(THROTTLE_MS)
        val orgsProsla = directoryRepository.getOrganizacije(prosla).getOrElse { emptyList() }
        val savezi = (orgsTekuca + orgsProsla).distinctBy { it.id }
        check(savezi.isNotEmpty()) { "Nijedan savez nije dostupan" }
        val saveziUTekucoj = orgsTekuca.map { it.id }.toSet()

        // Najnovija dostupna sezona po savezu: niže lige novu objavljuju kasnije.
        val lige = mutableListOf<LigaZaIndeks>()
        val kupovi = mutableListOf<LigaZaIndeks>()
        for (savez in savezi) {
            delay(THROTTLE_MS)
            var sezonaLige = tekuca
            var natjecanja = if (savez.id in saveziUTekucoj) {
                directoryRepository.getNatjecanja(savez.id, tekuca).getOrElse { emptyList() }
            } else {
                emptyList()
            }
            if (natjecanja.isEmpty()) {
                delay(THROTTLE_MS)
                sezonaLige = prosla
                natjecanja = directoryRepository.getNatjecanja(savez.id, prosla).getOrElse { emptyList() }
            }
            natjecanja
                .filter { SeniorskiFiltar.jeSeniorskaLiga(it.naziv) }
                .forEach { lige += LigaZaIndeks(it, savez.id, sezonaLige) }
            // Kupovi samo iz tekuće sezone — utakmice prošlosezonskih su odigrane.
            if (sezonaLige == tekuca) {
                natjecanja
                    .filter { SeniorskiFiltar.jeSeniorskiKup(it.naziv) }
                    .forEach { kupovi += LigaZaIndeks(it, savez.id, tekuca) }
            }
        }

        val jedinstveneLige = lige.distinctBy { it.natjecanje.id }
        val jedinstveniKupovi = kupovi.distinctBy { it.natjecanje.id }
        val ukupno = jedinstveneLige.size + jedinstveniKupovi.size
        var neuspjelih = 0

        jedinstveneLige.forEachIndexed { i, liga ->
            napredak(i, ukupno)
            if (jeSvjezaIndeksirana(liga)) return@forEachIndexed

            delay(THROTTLE_MS)
            natjecanjeRepository.getNatjecanjeStranica(liga.natjecanje.id)
                .onSuccess { stranica ->
                    dao.upsertKlubove(
                        stranica.klubovi.map { klub ->
                            KlubIndeksEntity(
                                klubId = klub.id,
                                naziv = klub.naziv,
                                nazivNorm = normalizirajZaPretragu(klub.naziv),
                                grbUrl = klub.grbUrl,
                                natjecanjeId = liga.natjecanje.id,
                                natjecanjeNaziv = liga.natjecanje.naziv.trim(),
                                savezId = liga.savezId,
                                sezona = liga.sezona,
                            )
                        },
                    )
                    dao.upsertIndeksiranaLiga(
                        IndeksiranaLigaEntity(liga.natjecanje.id, liga.sezona, System.currentTimeMillis()),
                    )
                }
                .onFailure { e ->
                    neuspjelih++
                    Log.w(TAG, "Liga ${liga.natjecanje.naziv} nije dohvaćena", e)
                }
        }

        check(neuspjelih <= jedinstveneLige.size / 2) {
            "Većina liga nije dohvaćena ($neuspjelih/${jedinstveneLige.size})"
        }
        postavkeRepository.postaviIndeksVerzija(SeniorskiFiltar.VERZIJA)

        val neuspjelihKupova = indeksirajKupove(jedinstveniKupovi) { i ->
            napredak(jedinstveneLige.size + i, ukupno)
        }
        napredak(ukupno, ukupno)

        // Uz pojedinačne neuspjehe pregled vrijedi — ti kupovi zadržavaju stari popis
        // sudionika. Ako je pala većina, pregled se ponavlja u sljedećem pokretanju.
        if (neuspjelihKupova <= jedinstveniKupovi.size / 2) {
            kupDao.obrisiDrugeSezone(tekuca)
            postavkeRepository.postaviPregledKupova(System.currentTimeMillis())
        }
    }

    private suspend fun jeSvjezaIndeksirana(liga: LigaZaIndeks): Boolean {
        val postojeca = dao.getIndeksiranaLiga(liga.natjecanje.id) ?: return false
        return postojeca.sezona == liga.sezona &&
            System.currentTimeMillis() - postojeca.azurirano < SVJEZINA_INDEKSA_MS
    }

    /**
     * Za svaki kup bilježi klubove koji u njemu imaju utakmicu — po tome dnevna
     * sinkronizacija zna u kojim kupovima praćeni klub igra. Klubovi viših rangova
     * ulaze tek u kasnijim rundama, pa se popis osvježava češće od liga. Vraća broj
     * kupova koji nisu dohvaćeni; oni zadržavaju stari popis sudionika.
     */
    private suspend fun indeksirajKupove(kupovi: List<LigaZaIndeks>, napredak: (Int) -> Unit): Int {
        var neuspjelih = 0
        kupovi.forEachIndexed { i, kup ->
            napredak(i)
            val postojeci = kupDao.getKup(kup.natjecanje.id)
            if (postojeci != null && System.currentTimeMillis() - postojeci.azurirano < SVJEZINA_KUPOVA_MS) {
                return@forEachIndexed
            }

            delay(THROTTLE_MS)
            natjecanjeRepository.getNatjecanjeStranica(kup.natjecanje.id)
                .onSuccess { stranica ->
                    // Popis klubova i utakmice se u pravilu poklapaju; unija štiti od
                    // izostanka jednog od njih u markupu.
                    val sudionici = (
                        stranica.klubovi.map { it.id } +
                            stranica.utakmice.flatMap { listOf(it.domacinId, it.gostId) }
                        )
                        .filter { it.isNotBlank() }
                        .toSet()
                    kupDao.zamijeniKup(
                        KupEntity(
                            natjecanjeId = kup.natjecanje.id,
                            naziv = kup.natjecanje.naziv.trim(),
                            savezId = kup.savezId,
                            sezona = kup.sezona,
                            azurirano = System.currentTimeMillis(),
                        ),
                        sudionici.map { KupSudionikEntity(kup.natjecanje.id, it) },
                    )
                }
                .onFailure { e ->
                    neuspjelih++
                    Log.w(TAG, "Kup ${kup.natjecanje.naziv} nije dohvaćen", e)
                }
        }
        return neuspjelih
    }

    private fun KlubIndeksEntity.toDomain() =
        KlubIndeks(klubId, naziv, grbUrl, natjecanjeId, natjecanjeNaziv)

    private data class LigaZaIndeks(
        val natjecanje: Natjecanje,
        val savezId: String,
        val sezona: String,
    )

    private companion object {
        const val TAG = "HnsKlubIndeksRepo"

        /** Pauza između zahtjeva prema semaforu — pristojnost prema izvoru. */
        const val THROTTLE_MS = 1_000L

        /** Klubovi se mijenjaju samo između sezona; mjesec dana je dovoljno svježe. */
        val SVJEZINA_INDEKSA_MS: Long = TimeUnit.DAYS.toMillis(30)

        /**
         * Sudionici kupa mijenjaju se svakim ždrijebom. Nešto kraće od tjednog ciklusa
         * indeksiranja, da svako tjedno pokretanje osvježi popis.
         */
        val SVJEZINA_KUPOVA_MS: Long = TimeUnit.DAYS.toMillis(6)
    }
}
