package com.navijacisazabranom.app.data.postavke

import kotlinx.coroutines.flow.Flow

interface PostavkeRepository {
    /** Večernji podsjetnik dan prije utakmice; isključen dok ga korisnik ne uključi. */
    fun observeVecernjiPodsjetnik(): Flow<Boolean>
    suspend fun getVecernjiPodsjetnik(): Boolean
    suspend fun postaviVecernjiPodsjetnik(ukljucen: Boolean)

    /** Kartica s uputama za pouzdan rad u pozadini; nakon odbacivanja se ne prikazuje. */
    fun observeKarticaPouzdanostiOdbacena(): Flow<Boolean>
    suspend fun odbaciKarticuPouzdanosti()

    /** Verzija sheme/filtra indeksa klubova — promjena okida čišćenje i ponovnu izgradnju. */
    suspend fun getIndeksVerzija(): Int
    suspend fun postaviIndeksVerzija(verzija: Int)

    /** Je li HNS logo okrenut naopako (easter egg triple-tap); trajno se pamti. */
    fun observeHnsNaopako(): Flow<Boolean>
    suspend fun preokreniHnsLogo()

    /** ID utakmice za koju je već prikazan trenutačni podsjetnik na dan utakmice (da se ne ponavlja). */
    suspend fun getZadnjaNotificiranaUtakmica(): String?
    suspend fun postaviZadnjaNotificiranaUtakmica(utakmicaId: String)
}
