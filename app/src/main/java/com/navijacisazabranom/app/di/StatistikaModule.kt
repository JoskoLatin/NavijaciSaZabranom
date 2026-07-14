package com.navijacisazabranom.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.navijacisazabranom.app.data.statistika.FirestoreStatistikaRepository
import com.navijacisazabranom.app.data.statistika.StatistikaRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StatistikaModule {

    @Binds
    @Singleton
    abstract fun bindStatistikaRepository(impl: FirestoreStatistikaRepository): StatistikaRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = Firebase.firestore
    }
}
