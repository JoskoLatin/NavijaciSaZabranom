package com.navijacisazabranom.app.di

import com.navijacisazabranom.app.data.matches.HnsMatchRepository
import com.navijacisazabranom.app.data.matches.MatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchModule {

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: HnsMatchRepository): MatchRepository
}
