package com.navijacisazabranom.app.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NavijaciDatabase =
        Room.databaseBuilder(context, NavijaciDatabase::class.java, "navijaci.db").build()
}
