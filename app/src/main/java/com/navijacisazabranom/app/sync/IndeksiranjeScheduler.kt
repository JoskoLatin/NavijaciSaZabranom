package com.navijacisazabranom.app.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Indeks svih klubova gradi se/osvježava u pozadini, jednom tjedno — popis
 * klubova se mijenja tek između sezona, a sudionici kupova sa svakim
 * ždrijebom. Worker sam preskače svježe lige i kupove, pa je većina tjednih
 * pokretanja jeftina. Prvo pokretanje (prazan indeks) WorkManager izvrši čim
 * su ispunjeni uvjeti (mreža), pa ekran za odabir kluba ne mora ništa okidati.
 */
object IndeksiranjeScheduler {
    private const val WORK_NAME = "izgradnja-indeksa-klubova"
    private const val WORK_NAME_DOPUNA = "dopuna-indeksa-klubova"
    private const val INTERVAL_DANA = 7L

    private val SAMO_S_MREZOM = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun zakazi(workManager: WorkManager) {
        val zahtjev = PeriodicWorkRequestBuilder<IndeksiranjeWorker>(INTERVAL_DANA, TimeUnit.DAYS)
            .setConstraints(SAMO_S_MREZOM)
            .build()

        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, zahtjev)
    }

    /**
     * Pri pokretanju aplikacije zastario indeks dopunjuje se odmah, a ne tek u
     * sljedećem tjednom ciklusu — npr. popis kupova odmah nakon ažuriranja na
     * verziju koja ih prati. Ako je indeks svjež, worker odmah završava.
     */
    fun dopuniOdmah(workManager: WorkManager) {
        val zahtjev = OneTimeWorkRequestBuilder<IndeksiranjeWorker>()
            .setConstraints(SAMO_S_MREZOM)
            .build()

        workManager.enqueueUniqueWork(WORK_NAME_DOPUNA, ExistingWorkPolicy.KEEP, zahtjev)
    }
}
