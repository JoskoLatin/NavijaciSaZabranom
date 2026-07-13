package com.navijacisazabranom.app.data.hns.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KlubIndeksDao {

    @Query(
        "SELECT * FROM klub_indeks WHERE nazivNorm LIKE '%' || :upitNorm || '%' " +
            "ORDER BY naziv LIMIT 50",
    )
    fun pretrazi(upitNorm: String): Flow<List<KlubIndeksEntity>>

    @Query("SELECT COUNT(*) FROM klub_indeks")
    fun observeBrojKlubova(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKlubove(klubovi: List<KlubIndeksEntity>)

    @Query("SELECT * FROM indeksirana_liga WHERE natjecanjeId = :natjecanjeId")
    suspend fun getIndeksiranaLiga(natjecanjeId: String): IndeksiranaLigaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIndeksiranaLiga(liga: IndeksiranaLigaEntity)

    @Query("SELECT MAX(azurirano) FROM indeksirana_liga")
    suspend fun zadnjeIndeksiranje(): Long?

    @Query("DELETE FROM klub_indeks")
    suspend fun obrisiSveKlubove()

    @Query("DELETE FROM indeksirana_liga")
    suspend fun obrisiSveIndeksiraneLige()
}
