package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class IndeksNapredak(val obradjeno: Int, val ukupno: Int)

/**
 * Napredak izgradnje indeksa u memoriji procesa — worker piše, tražilica
 * čita. Nakon smrti procesa napredak se gubi, ali worker svejedno nastavlja
 * gdje je stao (indeksirana_liga tablica), pa je ovo samo prikaz.
 */
@Singleton
class IndeksStanje @Inject constructor() {

    private val _napredak = MutableStateFlow<IndeksNapredak?>(null)
    val napredak: StateFlow<IndeksNapredak?> = _napredak.asStateFlow()

    fun postavi(obradjeno: Int, ukupno: Int) {
        _napredak.value = IndeksNapredak(obradjeno, ukupno)
    }

    fun zavrsi() {
        _napredak.value = null
    }
}
