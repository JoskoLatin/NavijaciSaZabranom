package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.flow.Flow

interface KlubIndeksRepository {
    /** Pretraga po imenu, neosjetljiva na velika/mala slova i dijakritike. */
    fun pretrazi(upit: String): Flow<List<KlubIndeks>>

    fun observeBrojKlubova(): Flow<Int>

    /** Indeks je prazan ili stariji od mjesec dana. */
    suspend fun indeksZastario(): Boolean

    /**
     * Gradi indeks svih seniorskih klubova: enumerira saveze (najnovija
     * dostupna sezona po savezu), filtrira seniorske lige i za svaku dohvaća
     * popis klubova. Već svježe indeksirane lige se preskaču, pa je poziv
     * siguran za ponavljanje i nastavlja prekinuti posao.
     */
    suspend fun izgradiIndeks(napredak: (obradjeno: Int, ukupno: Int) -> Unit): Result<Unit>
}
