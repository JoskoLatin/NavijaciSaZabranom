package com.navijacisazabranom.app.data.hns

import com.navijacisazabranom.app.data.hns.local.UtakmicaEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

private fun testUtakmica(id: String, datum: LocalDate, vrijeme: LocalTime?) = UtakmicaEntity(
    id = id,
    natjecanjeId = "n1",
    kolo = 1,
    datum = datum,
    vrijeme = vrijeme,
    domacinId = "hajduk",
    domacinNaziv = "Hajduk Split",
    gostId = "rijeka",
    gostNaziv = "Rijeka",
    stadion = null,
    rezultatDomacin = null,
    rezultatGost = null,
)

class PromjeneTerminaTest {

    private val danas = LocalDate.of(2026, 7, 12)

    @Test
    fun `objava satnice se detektira`() {
        val stara = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = null)
        val nova = stara.copy(vrijeme = LocalTime.of(18, 0))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(nova), danas)

        assertEquals(listOf(nova), promjene)
    }

    @Test
    fun `promjena satnice se detektira`() {
        val stara = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = LocalTime.of(18, 0))
        val nova = stara.copy(vrijeme = LocalTime.of(20, 30))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(nova), danas)

        assertEquals(listOf(nova), promjene)
    }

    @Test
    fun `promjena datuma se detektira`() {
        val stara = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = LocalTime.of(18, 0))
        val nova = stara.copy(datum = danas.plusDays(6))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(nova), danas)

        assertEquals(listOf(nova), promjene)
    }

    @Test
    fun `nepromijenjen termin se ignorira`() {
        val stara = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = LocalTime.of(18, 0))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(stara.copy()), danas)

        assertTrue(promjene.isEmpty())
    }

    @Test
    fun `prvi dohvat (prazan cache) ne javlja nista`() {
        val nova = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = LocalTime.of(18, 0))

        val promjene = pronadjiPromjeneTermina(emptyList(), listOf(nova), danas)

        assertTrue(promjene.isEmpty())
    }

    @Test
    fun `prosla utakmica se ignorira`() {
        val stara = testUtakmica(id = "1", datum = danas.minusDays(1), vrijeme = null)
        val nova = stara.copy(vrijeme = LocalTime.of(18, 0))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(nova), danas)

        assertTrue(promjene.isEmpty())
    }

    @Test
    fun `utakmica na danasnji dan se ne ignorira`() {
        val stara = testUtakmica(id = "1", datum = danas, vrijeme = LocalTime.of(18, 0))
        val nova = stara.copy(vrijeme = LocalTime.of(20, 0))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(nova), danas)

        assertEquals(listOf(nova), promjene)
    }

    @Test
    fun `utakmica koje nema u starom cacheu (drugi klub) se ignorira`() {
        val stara = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = null)
        val tudja = testUtakmica(id = "2", datum = danas.plusDays(5), vrijeme = LocalTime.of(18, 0))

        val promjene = pronadjiPromjeneTermina(listOf(stara), listOf(stara.copy(), tudja), danas)

        assertTrue(promjene.isEmpty())
    }

    @Test
    fun `vise promjena odjednom`() {
        val stara1 = testUtakmica(id = "1", datum = danas.plusDays(5), vrijeme = null)
        val stara2 = testUtakmica(id = "2", datum = danas.plusDays(12), vrijeme = null)
        val nova1 = stara1.copy(vrijeme = LocalTime.of(18, 0))
        val nova2 = stara2.copy(vrijeme = LocalTime.of(20, 30))

        val promjene = pronadjiPromjeneTermina(listOf(stara1, stara2), listOf(nova1, nova2), danas)

        assertEquals(listOf(nova1, nova2), promjene)
    }
}
