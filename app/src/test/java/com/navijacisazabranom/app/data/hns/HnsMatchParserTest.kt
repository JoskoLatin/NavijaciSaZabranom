package com.navijacisazabranom.app.data.hns

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
        val utakmice = HnsMatchParser.parseUtakmice(document)

        println("Ukupno utakmica (2026-27): ${utakmice.size}")
        println("Raspon kola: ${utakmice.minOf { it.kolo }}..${utakmice.maxOf { it.kolo }}")

        assertEquals("Ne smije biti duplikata po ID-u", utakmice.size, utakmice.distinctBy { it.id }.size)
        assertTrue("Raspored ne smije biti prazan", utakmice.isNotEmpty())
        assertTrue("Datumi moraju biti unutar sezone 2026/27", utakmice.all { it.datum.year == 2026 || it.datum.year == 2027 })
    }

    @Test
    fun `season not yet started has no published kickoff times`() {
        val document = loadFixture("hnl-2026-27.html")
        val utakmice = HnsMatchParser.parseUtakmice(document)

        assertTrue("2026-27 satnice još nisu objavljene (TBD)", utakmice.all { it.vrijeme == null })
        assertTrue("Neodigrane utakmice nemaju rezultat", utakmice.all { it.rezultatDomacin == null && it.rezultatGost == null })
    }

    @Test
    fun `filters Hajduk matches correctly from 2026-27 season`() {
        val document = loadFixture("hnl-2026-27.html")
        val utakmice = HnsMatchParser.parseUtakmice(document).filter { it.domacinId == HAJDUK_ID || it.gostId == HAJDUK_ID }

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
        val utakmice = HnsMatchParser.parseUtakmice(document)
        val odigrane = utakmice.filter { it.rezultatDomacin != null }

        println("Ukupno utakmica (2025-26): ${utakmice.size}, odigranih: ${odigrane.size}")

        assertTrue("Odigrana sezona mora imati utakmice s rezultatom", odigrane.isNotEmpty())
        assertTrue("Odigrana sezona mora imati objavljene satnice", utakmice.any { it.vrijeme != null })
        assertTrue("Odigrane utakmice moraju imati oba rezultata", odigrane.all { it.rezultatDomacin != null && it.rezultatGost != null })
    }

    @Test
    fun `parses club list for HNL competition`() {
        val document = loadFixture("hnl-2026-27.html")
        val klubovi = HnsMatchParser.parseKlubovi(document)

        println("Klubovi u HNL-u (2026-27): ${klubovi.size}")

        assertTrue("Mora postojati barem jedan klub", klubovi.isNotEmpty())
        assertTrue("Hajduk mora biti u popisu klubova", klubovi.any { it.id == HAJDUK_ID && it.naziv.contains("Hajduk") })
        assertEquals("Ne smije biti duplikata klubova", klubovi.size, klubovi.distinctBy { it.id }.size)
    }

    @Test
    fun `generalizes to a lower-tier county league page with published kickoff times`() {
        val document = loadFixture("znl-2026-27.html")
        val utakmice = HnsMatchParser.parseUtakmice(document)
        val klubovi = HnsMatchParser.parseKlubovi(document)

        println("Županijska liga - utakmica: ${utakmice.size}, klubova: ${klubovi.size}")

        assertTrue("Raspored ne smije biti prazan", utakmice.isNotEmpty())
        assertTrue("Popis klubova ne smije biti prazan", klubovi.isNotEmpty())
        assertTrue(
            "Županijska liga već ima objavljene satnice (za razliku od HNL-a)",
            utakmice.any { it.vrijeme != null },
        )
        assertEquals(
            "Ne smije biti duplikata iz scoreboard_first_line widgeta",
            utakmice.size,
            utakmice.distinctBy { it.id }.size,
        )
    }

    private companion object {
        const val HAJDUK_ID = "515"
    }
}
