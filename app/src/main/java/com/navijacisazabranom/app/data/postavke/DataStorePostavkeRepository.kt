package com.navijacisazabranom.app.data.postavke

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.postavkeDataStore by preferencesDataStore(name = "postavke")

class DataStorePostavkeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : PostavkeRepository {

    override fun observeVecernjiPodsjetnik(): Flow<Boolean> =
        context.postavkeDataStore.data.map { it[VECERNJI_PODSJETNIK] ?: false }

    override suspend fun getVecernjiPodsjetnik(): Boolean =
        observeVecernjiPodsjetnik().first()

    override suspend fun postaviVecernjiPodsjetnik(ukljucen: Boolean) {
        context.postavkeDataStore.edit { it[VECERNJI_PODSJETNIK] = ukljucen }
    }

    override fun observeKarticaPouzdanostiOdbacena(): Flow<Boolean> =
        context.postavkeDataStore.data.map { it[KARTICA_POUZDANOSTI_ODBACENA] ?: false }

    override suspend fun odbaciKarticuPouzdanosti() {
        context.postavkeDataStore.edit { it[KARTICA_POUZDANOSTI_ODBACENA] = true }
    }

    override suspend fun getIndeksVerzija(): Int =
        context.postavkeDataStore.data.map { it[INDEKS_VERZIJA] ?: 0 }.first()

    override suspend fun postaviIndeksVerzija(verzija: Int) {
        context.postavkeDataStore.edit { it[INDEKS_VERZIJA] = verzija }
    }

    override fun observeHnsNaopako(): Flow<Boolean> =
        context.postavkeDataStore.data.map { it[HNS_NAOPAKO] ?: false }

    override suspend fun preokreniHnsLogo() {
        context.postavkeDataStore.edit { it[HNS_NAOPAKO] = !(it[HNS_NAOPAKO] ?: false) }
    }

    override suspend fun getZadnjaNotificiranaUtakmica(): String? =
        context.postavkeDataStore.data.map { it[ZADNJA_NOTIFICIRANA] }.first()

    override suspend fun postaviZadnjaNotificiranaUtakmica(utakmicaId: String) {
        context.postavkeDataStore.edit { it[ZADNJA_NOTIFICIRANA] = utakmicaId }
    }

    override fun observeUKalendaru(): Flow<Map<String, Long>> =
        context.postavkeDataStore.data.map { dekodiraj(it[U_KALENDARU]) }

    override suspend fun zabiljeziUKalendaru(zapisi: Map<String, Long>) {
        context.postavkeDataStore.edit {
            it[U_KALENDARU] = kodiraj(dekodiraj(it[U_KALENDARU]) + zapisi)
        }
    }

    override suspend fun zadrziUKalendaru(utakmicaIds: Set<String>) {
        context.postavkeDataStore.edit {
            val zadrzani = dekodiraj(it[U_KALENDARU]).filterKeys { id -> id in utakmicaIds }
            it[U_KALENDARU] = kodiraj(zadrzani)
        }
    }

    /** Zapis para je "idUtakmice|idDogadjaja" — id utakmice nikad ne sadrži uspravnu crtu. */
    private fun kodiraj(zapisi: Map<String, Long>): Set<String> =
        zapisi.map { (utakmicaId, dogadjajId) -> "$utakmicaId$RAZDJELNIK$dogadjajId" }.toSet()

    private fun dekodiraj(zapisi: Set<String>?): Map<String, Long> =
        zapisi.orEmpty().mapNotNull { zapis ->
            val granica = zapis.lastIndexOf(RAZDJELNIK)
            if (granica <= 0) return@mapNotNull null
            val dogadjajId = zapis.substring(granica + 1).toLongOrNull() ?: return@mapNotNull null
            zapis.substring(0, granica) to dogadjajId
        }.toMap()

    private companion object {
        const val RAZDJELNIK = '|'

        val VECERNJI_PODSJETNIK = booleanPreferencesKey("vecernji_podsjetnik")
        val KARTICA_POUZDANOSTI_ODBACENA = booleanPreferencesKey("kartica_pouzdanosti_odbacena")
        val INDEKS_VERZIJA = intPreferencesKey("indeks_verzija")
        val HNS_NAOPAKO = booleanPreferencesKey("hns_naopako")
        val ZADNJA_NOTIFICIRANA = stringPreferencesKey("zadnja_notificirana_utakmica")
        val U_KALENDARU = stringSetPreferencesKey("utakmice_u_kalendaru")
    }
}
