package com.navijacisazabranom.app.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Indeks svih klubova gradi se/osvježava u pozadini, jednom tjedno — popis
 * klubova se mijenja tek između sezona, pa nema potrebe češće. Worker sam
 * preskače svježe lige, pa je većina tjednih pokretanja jeftina. Prvo
 * pokretanje (prazan indeks) WorkManager izvrši čim su ispunjeni uvjeti
 * (mreža), pa ekran za odabir kluba ne mora ništa okidati.
 */
object IndeksiranjeScheduler {
    private const val WORK_NAME = "izgradnja-indeksa-klubova"
    private const val INTERVAL_DANA = 7L

    fun zakazi(workManager: WorkManager) {
        val zahtjev = PeriodicWorkRequestBuilder<IndeksiranjeWorker>(INTERVAL_DANA, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, zahtjev)
    }
}
