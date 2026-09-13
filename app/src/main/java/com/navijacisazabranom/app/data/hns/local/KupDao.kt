package com.navijacisazabranom.app.data.hns.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface KupDao {

    @Query("SELECT * FROM kup WHERE klubId = :klubId AND sezona = :sezona ORDER BY natjecanjeId")
    suspend fun kupoviKluba(klubId: String, sezona: String): List<KupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKupove(kupovi: List<KupEntity>)

    @Query("DELETE FROM kup WHERE klubId = :klubId")
    suspend fun obrisiKupoveKluba(klubId: String)

    /** Zamjenjuje popis kupova kluba; kup iz kojeg je klub ispao tako nestaje s popisa. */
    @Transaction
    suspend fun zamijeniKupoveKluba(klubId: String, kupovi: List<KupEntity>) {
        obrisiKupoveKluba(klubId)
        insertKupove(kupovi)
    }

    /** Kupovi prošlih sezona više ne trebaju — njihove utakmice su odigrane. */
    @Query("DELETE FROM kup WHERE sezona != :sezona")
    suspend fun obrisiDrugeSezone(sezona: String)
}
