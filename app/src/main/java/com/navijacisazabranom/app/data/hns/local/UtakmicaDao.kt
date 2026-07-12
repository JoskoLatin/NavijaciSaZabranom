package com.navijacisazabranom.app.data.hns.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UtakmicaDao {
    @Query(
        "SELECT * FROM utakmica WHERE natjecanjeId = :natjecanjeId " +
            "AND (domacinId = :klubId OR gostId = :klubId) ORDER BY datum",
    )
    fun observeZaKlub(natjecanjeId: String, klubId: String): Flow<List<UtakmicaEntity>>

    @Query(
        "SELECT * FROM utakmica WHERE natjecanjeId = :natjecanjeId " +
            "AND (domacinId = :klubId OR gostId = :klubId) ORDER BY datum",
    )
    suspend fun getZaKlub(natjecanjeId: String, klubId: String): List<UtakmicaEntity>

    @Query("DELETE FROM utakmica WHERE natjecanjeId = :natjecanjeId")
    suspend fun obrisiZaNatjecanje(natjecanjeId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(utakmice: List<UtakmicaEntity>)

    @Transaction
    suspend fun zamijeniZaNatjecanje(natjecanjeId: String, utakmice: List<UtakmicaEntity>) {
        obrisiZaNatjecanje(natjecanjeId)
        insertAll(utakmice)
    }
}
