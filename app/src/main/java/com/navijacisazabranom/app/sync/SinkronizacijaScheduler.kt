package com.navijacisazabranom.app.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 2x dnevno je dovoljno cesto da uhvati objavu/promjenu satnice (obicno
 * ~2 tjedna unaprijed) bez agresivnog pollinga izvora - vidi napomenu o
 * pristojnosti u istrazivanju HNS Semafora. Dinamicko ucestalije dohvacanje
 * blizu dana utakmice nije dio ovog MVP-a.
 */
object SinkronizacijaScheduler {
    private const val WORK_NAME = "dnevna-sinkronizacija-rasporeda"
    private const val INTERVAL_HOURS = 12L

    fun zakazi(workManager: WorkManager) {
        val zahtjev = PeriodicWorkRequestBuilder<SinkronizacijaWorker>(INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, zahtjev)
    }
}
