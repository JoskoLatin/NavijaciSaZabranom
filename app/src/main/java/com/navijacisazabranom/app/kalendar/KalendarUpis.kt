package com.navijacisazabranom.app.kalendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Upis termina u kalendar uz evidenciju već upisanih — zajedničko za klupski
 * raspored i raspored reprezentacije. Evidencija je jedna mapa, pa se termini
 * reprezentacije razlikuju prefiksom ključa.
 */
class KalendarUpis @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postavkeRepository: PostavkeRepository,
) {

    /**
     * Upisuje termine kojih još nema u kalendaru; vraća koliko ih je upisano
     * (0 znači da su svi već ondje).
     */
    suspend fun dodajNove(utakmice: List<Utakmica>, opis: String, prefiks: String = ""): Result<Int> {
        // Bez ovoga bi termini koje je korisnik obrisao u kalendaru vrijedili kao već dodani.
        ocistiObrisane()

        val vec = postavkeRepository.observeUKalendaru().first()
        val zaDodati = utakmice.filter { prefiks + it.id !in vec }
        if (zaDodati.isEmpty()) return Result.success(0)

        return withContext(Dispatchers.IO) {
            KalendarPomocnik.dodajSve(context, zaDodati, opis) { prefiks + it.id }
        }
            .onSuccess { postavkeRepository.zabiljeziUKalendaru(it) }
            .map { it.size }
    }

    /**
     * Briše evidenciju za termine kojih više nema u kalendaru (korisnik ih je obrisao
     * ondje), da ih aplikacija ponovno ponudi umjesto da tvrdi da su već upisani.
     */
    suspend fun ocistiObrisane() {
        if (!smijeCitati()) return
        val zapisi = postavkeRepository.observeUKalendaru().first()
        if (zapisi.isEmpty()) return

        withContext(Dispatchers.IO) {
            KalendarPomocnik.postojeciTermini(context, zapisi.values.toSet())
        }.onSuccess { postojeci ->
            val preostali = zapisi.filterValues { it in postojeci }.keys
            if (preostali.size != zapisi.size) postavkeRepository.zadrziUKalendaru(preostali)
        }
    }

    private fun smijeCitati(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        /** Prefiks ključa za termine reprezentacije (klupski idu bez prefiksa). */
        const val PREFIKS_REPREZENTACIJA = "repre-"
    }
}
