package com.navijacisazabranom.app.data.hns

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HnsDirectoryParserTest {

    private fun loadFixture(name: String) = File("src/test/resources/$name").readText()

    @Test
    fun `parses organizations including HNS and county federations`() {
        val organizacije = HnsDirectoryParser.parseOrganizacije(loadFixture("organizations-2026-27.json"))

        assertTrue("Mora postojati barem jedna organizacija", organizacije.isNotEmpty())
        assertTrue(
            "HNS mora biti u popisu",
            organizacije.any { it.id == HNS_ID && it.naziv == "Hrvatski nogometni savez" },
        )
        assertTrue(
            "ŽNS zagrebački mora biti u popisu s ispravnom šifrom (dijakritici)",
            organizacije.any { it.id == "313" && it.naziv == "ŽNS zagrebački" },
        )
    }

    @Test
    fun `parses HNS competitions including all senior tiers`() {
        val natjecanja = HnsDirectoryParser.parseNatjecanja(loadFixture("competitions-hns-2026-27.json"))

        assertEquals("Fixture sadrži točno 4 natjecanja", 4, natjecanja.size)
        assertTrue("SuperSport HNL mora biti u popisu", natjecanja.any { it.id == "114137140" && it.naziv == "SuperSport HNL" })
        assertTrue("SuperSport Prva NL mora biti u popisu", natjecanja.any { it.naziv == "SuperSport Prva NL" })
        assertTrue("SuperSport Druga NL mora biti u popisu", natjecanja.any { it.naziv == "SuperSport Druga NL" })
    }

    @Test
    fun `parses county league competitions for a non-HNS organization`() {
        val natjecanja = HnsDirectoryParser.parseNatjecanja(loadFixture("competitions-zns-zagreb-2026-27.json"))

        assertTrue("ŽNS zagrebački mora imati natjecanja", natjecanja.isNotEmpty())
        assertTrue(
            "J1.ŽNL 26/27 mora biti u popisu (dijakritici ispravni)",
            natjecanja.any { it.naziv == "J1.ŽNL 26/27" },
        )
    }

    private companion object {
        const val HNS_ID = "1"
    }
}
