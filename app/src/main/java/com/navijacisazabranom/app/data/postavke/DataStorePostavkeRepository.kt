package com.navijacisazabranom.app.data.postavke

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    private companion object {
        val VECERNJI_PODSJETNIK = booleanPreferencesKey("vecernji_podsjetnik")
        val KARTICA_POUZDANOSTI_ODBACENA = booleanPreferencesKey("kartica_pouzdanosti_odbacena")
        val INDEKS_VERZIJA = intPreferencesKey("indeks_verzija")
    }
}
