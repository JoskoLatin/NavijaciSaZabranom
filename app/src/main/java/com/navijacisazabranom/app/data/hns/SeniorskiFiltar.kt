package com.navijacisazabranom.app.data.hns

/**
 * Za indeks klubova zanimaju nas samo seniorske lige velikog nogometa.
 * Nazivi na županijskoj razini su kaotični (dijalektalni nazivi mlađih
 * uzrasta: "TIĆI", "PAPALINE", "Karlići", "LIMAČI"), pa se filtrira
 * isključivanjem poznatih obrazaca na normaliziranom tekstu. Kupovi,
 * kvalifikacije i doigravanja se preskaču jer ne donose nove klubove, a
 * liga (ne kup) je ono što se prikazuje kao rang kluba.
 *
 * Seniorski kupovi se zasebno prepoznaju za raspored kluba: utakmice kupa
 * nisu na stranici lige, a obveza javljanja vrijedi i za njih.
 */
object SeniorskiFiltar {

    /**
     * Verzija filtra; promjena okida čišćenje i ponovnu izgradnju indeksa, jer
     * već indeksirane lige (evidentirane kao svježe) inače nikad ne bi bile
     * ponovno procijenjene poboljšanim filtrom. Povećati pri svakoj promjeni
     * uzoraka koja mijenja koje lige prolaze.
     */
    const val VERZIJA = 2

    /** Mlađi uzrasti (bilo koja U-dob: U-9, U11, U-12, U 14, ženski WU-13...) i dijalektalni nazivi. */
    private val MLADI = listOf(
        "junior", "kadet", "pionir", "limac", "pocetni", "mladez", "skolica",
        "tici", "papaline", "karlici", "zagici", """\bprst""", "spiget",
        """\bw?u ?-? ?\d{1,2}\b""", "prednatjecatelj",
    )

    /** Veterani (uklj. skraćeno "VET"). */
    private val VETERANI = listOf("""\bvet""")

    /** Mali nogomet / futsal (uklj. spojeni "mnkup"). */
    private val FUTSAL = listOf(
        "futsal", "hmnl", "hmnk", "shmnl", "malonogomet", "mali nogomet", "malom nogometu",
        """\bmznl\b""", """\bzmnl\b""", """\bzfl\b""", """\bmnkup""",
    )

    /** Kupovi (uklj. spojene: senkup, superkup — ali NE "skupina"). */
    private val KUP = listOf("""(?<!s)kup""", """\bcup\b""", """\bhnk\b""")

    /** Prijateljske utakmice i turniri nisu službena natjecanja. */
    private val NESLUZBENO = listOf("prijateljsk", "turnir")

    /** Kvalifikacije i doigravanja nisu liga u kojoj klub igra sezonu. */
    private val DOIGRAVANJA = listOf("kvalifikacij", "doigravanje", "zavrsnica")

    /** Ženski kup (HNKŽ, "žene", "ženski") — ženske ekipe su zasebni klubovi. */
    private val ZENSKO = listOf("""\bhnkz\b""", """\bzen(e|sk)""")

    private val ISKLJUCI_LIGU = regex(MLADI + VETERANI + FUTSAL + KUP + NESLUZBENO + DOIGRAVANJA)
    private val JE_KUP = regex(KUP)
    private val ISKLJUCI_KUP = regex(MLADI + VETERANI + FUTSAL + NESLUZBENO + ZENSKO)

    fun jeSeniorskaLiga(nazivNatjecanja: String): Boolean =
        !ISKLJUCI_LIGU.containsMatchIn(normalizirajZaPretragu(nazivNatjecanja))

    /** Kup velikog nogometa za seniore — Hrvatski nogometni kup ili kup saveza. */
    fun jeSeniorskiKup(nazivNatjecanja: String): Boolean {
        val naziv = normalizirajZaPretragu(nazivNatjecanja)
        return JE_KUP.containsMatchIn(naziv) && !ISKLJUCI_KUP.containsMatchIn(naziv)
    }

    private fun regex(uzorci: List<String>) = Regex(uzorci.joinToString("|"))
}
