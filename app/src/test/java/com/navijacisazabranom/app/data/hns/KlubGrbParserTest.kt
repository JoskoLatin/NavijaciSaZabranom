package com.navijacisazabranom.app.data.hns

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** parseKlubovi mora uz id i naziv izvući i URL grba (protiv stvarnog HTML-a). */
class KlubGrbParserTest {

    private fun ucitaj(naziv: String): Document {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream(naziv)) {
            "Nedostaje fixture $naziv"
        }
        return stream.use { Jsoup.parse(it, "UTF-8", "https://semafor.hns.family/") }
    }

    @Test
    fun `hnl klubovi imaju grbove`() {
        val klubovi = HnsMatchParser.parseKlubovi(ucitaj("hnl-2026-27.html"))

        assertEquals(10, klubovi.size)
        assertTrue(klubovi.all { !it.grbUrl.isNullOrBlank() })
        assertTrue(klubovi.all { it.grbUrl!!.startsWith("https://hns.family/") })

        val hajduk = klubovi.single { it.id == "515" }
        assertEquals("HNK Hajduk", hajduk.naziv)
    }

    @Test
    fun `i zupanijska liga ima grbove`() {
        val klubovi = HnsMatchParser.parseKlubovi(ucitaj("znl-2026-27.html"))

        assertEquals(16, klubovi.size)
        assertTrue(klubovi.all { !it.grbUrl.isNullOrBlank() })
    }
}
