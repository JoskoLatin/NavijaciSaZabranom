package com.navijacisazabranom.app.data.hns

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Fixture su stvarne ECL 2024/25 utakmice (Hajduk + Rijeka) s match.uefa.com. */
class UefaMatchParserTest {

    private val json = File("src/test/resources/uefa_ecl_2025.json").readText()
    private val naziv = "UEFA Konferencijska liga"

    @Test
    fun `vraca samo utakmice trazenog kluba (Hajduk, ne Rijeka)`() {
        val utakmice = UefaMatchParser.parse(json, naziv, klubNaziv = "HNK Hajduk", klubId = "515")

        assertEquals("Hajduk je u fixtureu odigrao 4 ECL utakmice", 4, utakmice.size)
        assertTrue(
            "Svaka utakmica mora uključivati Hajduk (UEFA ime 'Hajduk Split')",
            utakmice.all { it.domacinNaziv == "Hajduk Split" || it.gostNaziv == "Hajduk Split" },
        )
    }

    @Test
    fun `nasa strana dobiva HNS klubId, natjecanje i uefa prefiks`() {
        val utakmice = UefaMatchParser.parse(json, naziv, klubNaziv = "HNK Hajduk", klubId = "515")

        assertTrue(
            "Jedna strana mora imati HNS klubId da se uklopi u klupski upit",
            utakmice.all { it.domacinId == "515" || it.gostId == "515" },
        )
        assertTrue("Svaka nosi naziv natjecanja", utakmice.all { it.natjecanje == naziv })
        assertTrue("ID-jevi su prefiksirani da ne kolidiraju s HNS-om", utakmice.all { it.id.startsWith("uefa-") })
        assertTrue("Kolo je -1 (europske nemaju kola)", utakmice.all { it.kolo == -1 })
    }

    @Test
    fun `odigrane utakmice imaju rezultat`() {
        val utakmice = UefaMatchParser.parse(json, naziv, klubNaziv = "HNK Hajduk", klubId = "515")
        val odigrane = utakmice.filter { it.jeOdigrana() }
        assertTrue("Barem jedna utakmica u fixtureu je odigrana", odigrane.isNotEmpty())
        assertTrue(
            "Odigrane imaju rezultat oba tima",
            odigrane.all { it.rezultatDomacin != null && it.rezultatGost != null },
        )
    }

    @Test
    fun `podudaranje imena radi i za drugi klub (Rijeka)`() {
        val rijeka = UefaMatchParser.parse(json, naziv, klubNaziv = "HNK Rijeka", klubId = "999")
        assertTrue("Rijeka ima svoje utakmice u fixtureu", rijeka.isNotEmpty())
        assertTrue(rijeka.all { it.domacinNaziv == "Rijeka" || it.gostNaziv == "Rijeka" })
    }
}
