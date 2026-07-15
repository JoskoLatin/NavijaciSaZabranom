package com.navijacisazabranom.app.data.hns.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PraceniKlubEntity::class,
        UtakmicaEntity::class,
        KlubIndeksEntity::class,
        IndeksiranaLigaEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class NavijaciDatabase : RoomDatabase() {
    abstract fun praceniKlubDao(): PraceniKlubDao
    abstract fun utakmicaDao(): UtakmicaDao
    abstract fun klubIndeksDao(): KlubIndeksDao
}
