package com.navijacisazabranom.app.data.hns

interface ReprezentacijaRepository {
    /** Dohvaća puni raspored A reprezentacije s mreže (bez cachea — informativno, bez notifikacija). */
    suspend fun getRaspored(): Result<List<Utakmica>>
}
