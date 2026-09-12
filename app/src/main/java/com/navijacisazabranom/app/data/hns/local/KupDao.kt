package com.navijacisazabranom.app.data.hns.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface KupDao {

    /**
     * Kupovi u kojima klub igra ili može igrati: Hrvatski kup (za svaki klub), kupovi
     * saveza kojem pripada klupska liga (klub ondje može ući tek u kasnijoj rundi) i
     * kupovi u kojima klub već ima utakmicu.
     */
    @Query(
        "SELECT * FROM kup WHERE sezona = :sezona AND (" +
            "savezId = :nacionalniSavezId " +
            "OR savezId IN (SELECT savezId FROM klub_indeks WHERE klubId = :klubId) " +
            "OR natjecanjeId IN (SELECT kupId FROM kup_sudionik WHERE klubId = :klubId)" +
            ") ORDER BY natjecanjeId",
    )
    suspend fun kupoviZaKlub(klubId: String, sezona: String, nacionalniSavezId: String): List<KupEntity>

    @Query("SELECT * FROM kup WHERE savezId = :savezId AND sezona = :sezona")
    suspend fun kupoviSaveza(savezId: String, sezona: String): List<KupEntity>

    @Query("SELECT * FROM kup WHERE natjecanjeId = :natjecanjeId")
    suspend fun getKup(natjecanjeId: String): KupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKup(kup: KupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSudionike(sudionici: List<KupSudionikEntity>)

    @Query("DELETE FROM kup_sudionik WHERE kupId = :kupId")
    suspend fun obrisiSudionike(kupId: String)

    /** Sprema kup sa svježim popisom sudionika (stari popis se u cijelosti zamjenjuje). */
    @Transaction
    suspend fun zamijeniKup(kup: KupEntity, sudionici: List<KupSudionikEntity>) {
        upsertKup(kup)
        obrisiSudionike(kup.natjecanjeId)
        insertSudionike(sudionici)
    }

    @Query("DELETE FROM kup_sudionik WHERE kupId IN (SELECT natjecanjeId FROM kup WHERE sezona != :sezona)")
    suspend fun obrisiSudionikeDrugihSezona(sezona: String)

    @Query("DELETE FROM kup WHERE sezona != :sezona")
    suspend fun obrisiKupoveDrugihSezona(sezona: String)

    /** Kupovi prošlih sezona više ne trebaju — njihove utakmice su odigrane. */
    @Transaction
    suspend fun obrisiDrugeSezone(sezona: String) {
        obrisiSudionikeDrugihSezona(sezona)
        obrisiKupoveDrugihSezona(sezona)
    }
}
