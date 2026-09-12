package com.navijacisazabranom.app.data.hns

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * Stranice kupova (snimljene 11.09.2026.): Hrvatski kup nakon 1/16 finala i kup
 * Nogometnog saveza Zagrebačke županije nakon 1/8 finala.
 */
class HnsKupParserTest {

    private fun loadFixture(name: String) =
        Jsoup.parse(File("src/test/resources/$name"), "UTF-8")

    private val hnk by lazy { loadFixture("hnk-2026-27.html") }
    private val zupanijski by lazy { loadFixture("kup-nszz-2026-27.html") }

    @Test
    fun `kup ima isti markup kao liga, a runde umjesto kola`() {
        val utakmice = HnsMatchParser.parseUtakmice(hnk)
        val runde = HnsMatchParser.parseRunde(hnk)

        assertEquals("16 utakmica 1/32 i 16 utakmica 1/16 finala, bez duplikata iz widgeta", 32, utakmice.size)
        assertEquals("1/32 finala", runde[10002])
        assertEquals("1/16 finala", runde[10003])
        assertTrue("Svaka utakmica mora imati naziv runde", utakmice.all { it.kolo in runde })
    }

    @Test
    fun `utakmica kluba u kupu pronalazi se po istom id-u kao u ligi`() {
        val hajduk = HnsMatchParser.parseUtakmice(hnk)
            .filter { it.domacinId == HAJDUK_ID || it.gostId == HAJDUK_ID }

        assertEquals(1, hajduk.size)
        with(hajduk.single()) {
            assertEquals("NK Vardarac", domacinNaziv)
            assertEquals(LocalDate.of(2026, 9, 8), datum)
            assertEquals(LocalTime.of(16, 30), vrijeme)
            assertEquals(0, rezultatDomacin)
            assertEquals(2, rezultatGost)
        }
    }

    @Test
    fun `nadolazeca utakmica kupa nema rezultat`() {
        val rijeka = HnsMatchParser.parseUtakmice(hnk)
            .single { it.domacinId == RIJEKA_ID || it.gostId == RIJEKA_ID }

        assertEquals("NK Vrbovec", rijeka.domacinNaziv)
        assertEquals(LocalDate.of(2026, 9, 16), rijeka.datum)
        assertNull(rijeka.rezultatDomacin)
        assertNull(rijeka.rezultatGost)
    }

    @Test
    fun `zupanijski kup - klub koji ulazi u kasnijoj rundi je medju sudionicima`() {
        val utakmice = HnsMatchParser.parseUtakmice(zupanijski)
        val runde = HnsMatchParser.parseRunde(zupanijski)
        val klubovi = HnsMatchParser.parseKlubovi(zupanijski)

        assertEquals("1/16 finala", runde[10003])
        assertEquals("1/8 finala", runde[10004])
        // Vrbovec igra 4. NL, pa u županijski kup ulazi tek u 1/8 finala.
        val vrbovec = utakmice.filter { it.domacinId == VRBOVEC_ID || it.gostId == VRBOVEC_ID }
        assertEquals(listOf(10004), vrbovec.map { it.kolo })
        assertTrue("Vrbovec mora biti u popisu klubova kupa", klubovi.any { it.id == VRBOVEC_ID })
    }

    @Test
    fun `liga zadrzava nazive kola`() {
        val runde = HnsMatchParser.parseRunde(loadFixture("hnl-2026-27.html"))

        assertEquals(36, runde.size)
        assertEquals("1. kolo", runde[1])
        assertEquals("36. kolo", runde[36])
    }

    @Test
    fun `oznaka kupa u rasporedu`() {
        val hrvatski = Kup(id = "114492931", naziv = "SuperSport HNK", savezId = HnsConfig.HNS_SAVEZ_ID)
        val zupanijskiKup = Kup(id = "114669107", naziv = "KUP NSZŽ 26/27", savezId = "313")

        assertEquals("Hrvatski kup, 1/16 finala", hrvatski.oznaka("1/16 finala"))
        assertEquals("Županijski kup, 1/8 finala", zupanijskiKup.oznaka("1/8 finala"))
        assertEquals("Hrvatski kup", hrvatski.oznaka(null))
    }

    private companion object {
        const val HAJDUK_ID = "515"
        const val RIJEKA_ID = "1471"
        const val VRBOVEC_ID = "1306"
    }
}
