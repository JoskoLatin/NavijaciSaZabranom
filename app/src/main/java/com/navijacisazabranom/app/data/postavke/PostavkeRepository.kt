package com.navijacisazabranom.app.data.postavke

import kotlinx.coroutines.flow.Flow

interface PostavkeRepository {
    /** Večernji podsjetnik dan prije utakmice; isključen dok ga korisnik ne uključi. */
    fun observeVecernjiPodsjetnik(): Flow<Boolean>
    suspend fun getVecernjiPodsjetnik(): Boolean
    suspend fun postaviVecernjiPodsjetnik(ukljucen: Boolean)
}
