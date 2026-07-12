package com.navijacisazabranom.app.di

import com.navijacisazabranom.app.data.postavke.DataStorePostavkeRepository
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PostavkeModule {

    @Binds
    @Singleton
    abstract fun bindPostavkeRepository(impl: DataStorePostavkeRepository): PostavkeRepository
}
