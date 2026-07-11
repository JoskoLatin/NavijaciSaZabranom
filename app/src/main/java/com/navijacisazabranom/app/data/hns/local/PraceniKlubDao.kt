package com.navijacisazabranom.app.data.hns.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PraceniKlubDao {
    @Query("SELECT * FROM praceni_klub WHERE id = 1")
    fun observe(): Flow<PraceniKlubEntity?>

    @Query("SELECT * FROM praceni_klub WHERE id = 1")
    suspend fun get(): PraceniKlubEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun postavi(entity: PraceniKlubEntity)
}
