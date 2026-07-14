package com.navijacisazabranom.app.data.hns

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Fixture su stvarni redovi s rezultati.hns.team/?cid=FC A (dohvaćeno 2026-07-13). */
class HnsReprezentacijaParserTest {

    private val document = Jsoup.parse(File("src/test/resources/reprezentacija_2026.html"), "UTF-8")
    private val utakmice = HnsMatchParser.parseReprezentacija(document)

    @Test
    fun `parsira sve reprezentacijske utakmice bez duplikata`() {
        assertTrue("Raspored ne smije biti prazan", utakmice.isNotEmpty())
        assertEquals("Bez duplikata po ID-u", utakmice.size, utakmice.distinctBy { it.id }.size)
    }

    @Test
    fun `oba imena su popunjena i kad protivnicka celija nema anchor`() {
        // Ključna razlika naspram klupskog markupa: samo Hrvatska ćelija ima <a>.
        assertTrue(
            "Svaka utakmica mora imati oba imena",
            utakmice.all { it.domacinNaziv.isNotBlank() && it.gostNaziv.isNotBlank() },
        )
        assertTrue(
            "Hrvatska mora biti domaćin ili gost u svakoj utakmici",
            utakmice.all { it.domacinId == "70000" || it.gostId == "70000" },
        )
    }

    @Test
    fun `citaju se datum, satnica, naziv natjecanja i stadion`() {
        val ceska = utakmice.first { it.gostId == "72199" && it.domacinId == "70000" }
        assertEquals("Hrvatska", ceska.domacinNaziv)
        assertEquals("Češka", ceska.gostNaziv)
        assertEquals("Liga nacija - Skupina A", ceska.natjecanje)
        assertEquals(2026, ceska.datum.year)
        assertEquals(11, ceska.datum.monthValue)
        assertEquals(15, ceska.datum.dayOfMonth)
        assertEquals("20:45", ceska.vrijeme.toString())
        // Stadion na rezultati.hns.team je "-" → tretira se kao nepoznat.
        assertNull(ceska.stadion)
    }
}
