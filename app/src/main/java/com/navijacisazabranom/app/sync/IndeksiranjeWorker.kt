package com.navijacisazabranom.app.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.navijacisazabranom.app.data.hns.IndeksStanje
import com.navijacisazabranom.app.data.hns.KlubIndeksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Jednokratna izgradnja (ili mjesečno osvježavanje) indeksa svih seniorskih
 * klubova. Repository preskače svježe lige, pa retry nastavlja gdje je stao.
 */
@HiltWorker
class IndeksiranjeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val klubIndeksRepository: KlubIndeksRepository,
    private val indeksStanje: IndeksStanje,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!klubIndeksRepository.indeksZastario()) return Result.success()

        indeksStanje.postavi(obradjeno = 0, ukupno = 0)
        val rezultat = klubIndeksRepository.izgradiIndeks { obradjeno, ukupno ->
            indeksStanje.postavi(obradjeno, ukupno)
        }
        indeksStanje.zavrsi()

        return rezultat.fold(
            onSuccess = { Result.success() },
            onFailure = { e ->
                Log.w(TAG, "Izgradnja indeksa klubova neuspješna, pokušat će se ponovno", e)
                if (runAttemptCount < MAX_POKUSAJA) Result.retry() else Result.failure()
            },
        )
    }

    private companion object {
        const val TAG = "IndeksiranjeWorker"
        const val MAX_POKUSAJA = 3
    }
}
