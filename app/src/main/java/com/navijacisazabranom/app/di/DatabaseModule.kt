package com.navijacisazabranom.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.navijacisazabranom.app.data.hns.local.NavijaciDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `klub_indeks` (" +
                    "`klubId` TEXT NOT NULL, `naziv` TEXT NOT NULL, `nazivNorm` TEXT NOT NULL, " +
                    "`grbUrl` TEXT, `natjecanjeId` TEXT NOT NULL, `natjecanjeNaziv` TEXT NOT NULL, " +
                    "`savezId` TEXT NOT NULL, `sezona` TEXT NOT NULL, PRIMARY KEY(`klubId`))",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_klub_indeks_nazivNorm` ON `klub_indeks` (`nazivNorm`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `indeksirana_liga` (" +
                    "`natjecanjeId` TEXT NOT NULL, `sezona` TEXT NOT NULL, " +
                    "`azurirano` INTEGER NOT NULL, PRIMARY KEY(`natjecanjeId`))",
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NavijaciDatabase =
        Room.databaseBuilder(context, NavijaciDatabase::class.java, "navijaci.db")
            .addMigrations(MIGRATION_1_2)
            .build()
}
