package com.navijacisazabranom.app.data.hns

import android.util.Log
import com.navijacisazabranom.app.data.hns.local.KupEntity
import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import javax.inject.Inject

class HnsKupRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val directoryRepository: DirectoryRepository,
    private val natjecanjeRepository: NatjecanjeRepository,
    private val postavkeRepository: PostavkeRepository,
) : KupRepository {

    private val dao get() = database.kupDao()

    /**
     * Popis natjecanja kluba na semaforu odmah pokazuje i kup u koji je klub tek
     * izvučen, pa se pita pri svakoj sinkronizaciji. Ako upit padne, ostaje zapamćeni
     * popis — bolje ponovno dohvatiti stari kup nego ostati bez njega.
     */
    override suspend fun kupoviZaKlub(klubId: String): List<Kup> {
        val sezona = HnsConfig.tekucaSezona()
        val zapamceni = dao.kupoviKluba(klubId, sezona)

        directoryRepository.getNatjecanjaKluba(klubId, sezona)
            .onSuccess { natjecanja ->
                val nacionalni = nacionalniKupovi(sezona)
                val kupovi = natjecanja
                    .filter { SeniorskiFiltar.jeSeniorskiKup(it.naziv) }
                    .map { kup ->
                        KupEntity(
                            klubId = klubId,
                            natjecanjeId = kup.id,
                            naziv = kup.naziv.trim(),
                            sezona = sezona,
                            // Bez imenika HNS-a (npr. pao je upit) zadrži staru oznaku.
                            nacionalni = if (nacionalni.isEmpty()) {
                                zapamceni.firstOrNull { it.natjecanjeId == kup.id }?.nacionalni ?: false
                            } else {
                                kup.id in nacionalni
                            },
                        )
                    }
                dao.zamijeniKupoveKluba(klubId, kupovi)
                dao.obrisiDrugeSezone(sezona)
            }
            .onFailure { Log.w(TAG, "Popis natjecanja kluba nije dohvaćen, vrijedi zapamćeni", it) }

        return dao.kupoviKluba(klubId, sezona).map { Kup(it.natjecanjeId, it.naziv, it.nacionalni) }
    }

    override suspend fun getUtakmiceKluba(kup: Kup, klubId: String): Result<List<Utakmica>> =
        natjecanjeRepository.getNatjecanjeStranica(kup.id).map { stranica ->
            stranica.utakmice
                .filter { it.domacinId == klubId || it.gostId == klubId }
                .map { it.copy(natjecanje = kup.oznaka(stranica.runde[it.kolo])) }
        }

    /**
     * Hrvatski kup se prepoznaje po id-u iz imenika HNS-a, ne po nazivu: i kup
     * primorsko-goranskog saveza zove se "Hrvatski nogometni kup". Imenik se pita
     * jednom po sezoni, a odgovor se pamti.
     */
    private suspend fun nacionalniKupovi(sezona: String): Set<String> {
        if (postavkeRepository.getNacionalniKupSezona() == sezona) {
            val zapamceni = postavkeRepository.getNacionalniKupIds()
            if (zapamceni.isNotEmpty()) return zapamceni
        }

        val natjecanja = directoryRepository.getNatjecanja(HnsConfig.HNS_SAVEZ_ID, sezona)
            .onFailure { Log.w(TAG, "Imenik HNS-a nije dohvaćen", it) }
            .getOrElse { return emptySet() }

        val ids = natjecanja.filter { SeniorskiFiltar.jeSeniorskiKup(it.naziv) }.map { it.id }.toSet()
        if (ids.isNotEmpty()) postavkeRepository.postaviNacionalniKup(sezona, ids)
        return ids
    }

    private companion object {
        const val TAG = "HnsKupRepo"
    }
}
