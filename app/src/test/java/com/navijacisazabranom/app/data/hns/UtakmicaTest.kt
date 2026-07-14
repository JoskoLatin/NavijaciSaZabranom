package com.navijacisazabranom.app.data.hns

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class UtakmicaTest {

    private fun utakmica(
        datum: LocalDate,
        rezultatDomacin: Int? = null,
        rezultatGost: Int? = null,
    ) = Utakmica(
        id = "1",
        kolo = 1,
        datum = datum,
        vrijeme = LocalTime.NOON,
        domacinId = "1",
        domacinNaziv = "A",
        gostId = "2",
        gostNaziv = "B",
        stadion = null,
        rezultatDomacin = rezultatDomacin,
        rezultatGost = rezultatGost,
    )

    private val danas = LocalDate.of(2026, 7, 14)

    @Test
    fun `utakmica s rezultatom je odigrana`() {
        assertTrue(utakmica(danas, rezultatDomacin = 2, rezultatGost = 1).jeOdigrana(danas))
    }

    @Test
    fun `utakmica u proslosti je odigrana i bez rezultata`() {
        assertTrue(utakmica(danas.minusDays(1)).jeOdigrana(danas))
    }

    @Test
    fun `buduca utakmica bez rezultata nije odigrana`() {
        assertFalse(utakmica(danas.plusDays(1)).jeOdigrana(danas))
    }

    @Test
    fun `danasnja utakmica bez rezultata jos nije odigrana`() {
        assertFalse(utakmica(danas).jeOdigrana(danas))
    }
}
