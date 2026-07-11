package com.navijacisazabranom.app.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Osvježava raspored praćenog kluba u pozadini. Ako korisnik još nije
 * odabrao klub, nema što sinkronizirati - uspješan no-op.
 */
@HiltWorker
class SinkronizacijaWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val praceniKlubRepository: PraceniKlubRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val praceniKlub = praceniKlubRepository.getPraceniKlub() ?: return Result.success()

        return praceniKlubRepository.osvjeziUtakmice(praceniKlub.natjecanjeId).fold(
            onSuccess = { Result.success() },
            onFailure = { e ->
                Log.w(TAG, "Pozadinska sinkronizacija neuspješna, pokušat će se ponovno", e)
                Result.retry()
            },
        )
    }

    private companion object {
        const val TAG = "SinkronizacijaWorker"
    }
}
