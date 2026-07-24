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

    /**
     * Utakmice upisane u kalendar: id utakmice → id događaja u kalendaru. Spriječava
     * duplikate i otkriva nove termine, a id događaja omogućuje provjeru je li korisnik
     * termin obrisao izravno u kalendaru.
     */
    fun observeUKalendaru(): Flow<Map<String, Long>>
    suspend fun zabiljeziUKalendaru(zapisi: Map<String, Long>)

    /** Zadržava samo navedene utakmice — briše zapise za termine kojih više nema u kalendaru. */
    suspend fun zadrziUKalendaru(utakmicaIds: Set<String>)

    /** Vrijeme zadnje promjene profilne slike (0 = nema slike); mijenja se i radi osvježavanja prikaza. */
    fun observeProfilnaAzurirana(): Flow<Long>
    suspend fun postaviProfilnaAzurirana(vrijeme: Long)
}
