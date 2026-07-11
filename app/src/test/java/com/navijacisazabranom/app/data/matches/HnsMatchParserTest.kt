package com.navijacisazabranom.app.data.matches

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HnsMatchParserTest {

    private fun loadFixture(name: String) =
        Jsoup.parse(File("src/test/resources/$name"), "UTF-8")

    @Test
    fun `parses full season without duplicates from per-club widgets`() {
        val document = loadFixture("hnl-2026-27.html")
        val utakmice = HnsMatchParser.parse(document)

        println("Ukupno utakmica (2026-27): ${utakmice.size}")
        println("Raspon kola: ${utakmice.minOf { it.kolo }}..${utakmice.maxOf { it.kolo }}")

        assertEquals("Ne smije biti duplikata po ID-u", utakmice.size, utakmice.distinctBy { it.id }.size)
        assertTrue("Raspored ne smije biti prazan", utakmice.isNotEmpty())
        assertTrue("Datumi moraju biti unutar sezone 2026/27", utakmice.all { it.datum.year == 2026 || it.datum.year == 2027 })
    }

    @Test
    fun `season not yet started has no published kickoff times`() {
        val document = loadFixture("hnl-2026-27.html")
        val utakmice = HnsMatchParser.parse(document)

        assertTrue("2026-27 satnice još nisu objavljene (TBD)", utakmice.all { it.vrijeme == null })
        assertTrue("Neodigrane utakmice nemaju rezultat", utakmice.all { it.rezultatDomacin == null && it.rezultatGost == null })
    }

    @Test
    fun `filters Hajduk matches correctly from 2026-27 season`() {
        val document = loadFixture("hnl-2026-27.html")
        val utakmice = HnsMatchParser.parse(document).filter { it.domacinId == HAJDUK_ID || it.gostId == HAJDUK_ID }

        println("Hajduk utakmica (2026-27): ${utakmice.size}")
        utakmice.take(3).forEach {
            println("  ${it.kolo}. kolo ${it.datum} ${it.domacinNaziv} - ${it.gostNaziv}")
        }

        assertTrue("Hajduk mora imati barem jednu utakmicu u rasporedu", utakmice.isNotEmpty())
        assertTrue("Svaka utakmica mora uključivati Hajduk", utakmice.all { it.domacinId == HAJDUK_ID || it.gostId == HAJDUK_ID })
        assertTrue("Nazivi klubova ne smiju biti prazni", utakmice.none { it.domacinNaziv.isBlank() || it.gostNaziv.isBlank() })
    }

    @Test
    fun `parses played matches and kickoff times from finished 2025-26 season`() {
        val document = loadFixture("hnl-2025-26.html")
        val utakmice = HnsMatchParser.parse(document)
        val odigrane = utakmice.filter { it.rezultatDomacin != null }

        println("Ukupno utakmica (2025-26): ${utakmice.size}, odigranih: ${odigrane.size}")

        assertTrue("Odigrana sezona mora imati utakmice s rezultatom", odigrane.isNotEmpty())
        assertTrue("Odigrana sezona mora imati objavljene satnice", utakmice.any { it.vrijeme != null })
        assertTrue("Odigrane utakmice moraju imati oba rezultata", odigrane.all { it.rezultatDomacin != null && it.rezultatGost != null })
    }

    private companion object {
        const val HAJDUK_ID = "515"
    }
}
