package com.navijacisazabranom.app.data.statistika

interface StatistikaRepository {
    /**
     * Bilježi da je korisnik odabrao klub (anonimna statistika: uid + klub + vrijeme).
     * Fire-and-forget — ne smije blokirati ni rušiti korisnički tok.
     */
    fun zabiljeziOdabirKluba(klubId: String, klubNaziv: String, natjecanje: String)
}
