package com.navijacisazabranom.app.di

import com.navijacisazabranom.app.data.hns.DirectoryRepository
import com.navijacisazabranom.app.data.hns.HnsDirectoryRepository
import com.navijacisazabranom.app.data.hns.HnsKlubIndeksRepository
import com.navijacisazabranom.app.data.hns.HnsNatjecanjeRepository
import com.navijacisazabranom.app.data.hns.KlubIndeksRepository
import com.navijacisazabranom.app.data.hns.NatjecanjeRepository
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import com.navijacisazabranom.app.data.hns.RoomPraceniKlubRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CompetitionModule {

    @Binds
    @Singleton
    abstract fun bindNatjecanjeRepository(impl: HnsNatjecanjeRepository): NatjecanjeRepository

    @Binds
    @Singleton
    abstract fun bindDirectoryRepository(impl: HnsDirectoryRepository): DirectoryRepository

    @Binds
    @Singleton
    abstract fun bindPraceniKlubRepository(impl: RoomPraceniKlubRepository): PraceniKlubRepository

    @Binds
    @Singleton
    abstract fun bindKlubIndeksRepository(impl: HnsKlubIndeksRepository): KlubIndeksRepository
}
