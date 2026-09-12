package com.navijacisazabranom.app.data.hns

import android.util.Log
import com.navijacisazabranom.app.data.hns.local.KupEntity
import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import javax.inject.Inject

class HnsKupRepository @Inject constructor(
    private val database: NavijaciDatabase,
    private val directoryRepository: DirectoryRepository,
    private val natjecanjeRepository: NatjecanjeRepository,
) : KupRepository {

    private val dao get() = database.kupDao()

    override suspend fun kupoviZaKlub(klubId: String): List<Kup> {
        val sezona = HnsConfig.tekucaSezona()
        osigurajNacionalniKup(sezona)
        return dao.kupoviZaKlub(klubId, sezona, HnsConfig.HNS_SAVEZ_ID).map { it.toDomain() }
    }

    override suspend fun getUtakmiceKluba(kup: Kup, klubId: String): Result<List<Utakmica>> =
        natjecanjeRepository.getNatjecanjeStranica(kup.id).map { stranica ->
            stranica.utakmice
                .filter { it.domacinId == klubId || it.gostId == klubId }
                .map { it.copy(natjecanje = kup.oznaka(stranica.runde[it.kolo])) }
        }

    /**
     * Hrvatski kup provjerava se za svaki klub, pa ne smije čekati tjedno indeksiranje
     * kupova (npr. odmah nakon ažuriranja aplikacije). Ako ga još nema, pronađe se u
     * imeniku HNS-a jednim malim upitom; sudionike kasnije dopuni indeksiranje.
     */
    private suspend fun osigurajNacionalniKup(sezona: String) {
        if (dao.kupoviSaveza(HnsConfig.HNS_SAVEZ_ID, sezona).isNotEmpty()) return

        directoryRepository.getNatjecanja(HnsConfig.HNS_SAVEZ_ID, sezona)
            .onSuccess { natjecanja ->
                natjecanja
                    .filter { SeniorskiFiltar.jeSeniorskiKup(it.naziv) }
                    .forEach { kup ->
                        dao.upsertKup(
                            KupEntity(
                                natjecanjeId = kup.id,
                                naziv = kup.naziv.trim(),
                                savezId = HnsConfig.HNS_SAVEZ_ID,
                                sezona = sezona,
                                azurirano = 0,
                            ),
                        )
                    }
            }
            .onFailure { Log.w(TAG, "Imenik HNS-a nije dohvaćen, Hrvatski kup se preskače", it) }
    }

    private fun KupEntity.toDomain() = Kup(natjecanjeId, naziv, savezId)

    private companion object {
        const val TAG = "HnsKupRepo"
    }
}
