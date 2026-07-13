package com.navijacisazabranom.app.data.hns

/**
 * Za indeks klubova zanimaju nas samo seniorske lige velikog nogometa.
 * Nazivi na županijskoj razini su kaotični (dijalektalni nazivi mlađih
 * uzrasta: "TIĆI", "PAPALINE", "Karlići", "LIMAČI"), pa se filtrira
 * isključivanjem poznatih obrazaca na normaliziranom tekstu. Kupovi,
 * kvalifikacije i doigravanja se preskaču jer ne donose nove klubove, a
 * liga (ne kup) je ono što se prikazuje kao rang kluba.
 */
object SeniorskiFiltar {

    /**
     * Verzija filtra; promjena okida čišćenje i ponovnu izgradnju indeksa, jer
     * već indeksirane lige (evidentirane kao svježe) inače nikad ne bi bile
     * ponovno procijenjene poboljšanim filtrom. Povećati pri svakoj promjeni
     * uzoraka ispod.
     */
    const val VERZIJA = 2

    private val ISKLJUCI = Regex(
        listOf(
            // mlađi uzrasti (bilo koja U-dob: U-9, U11, U-12, U 14, ženski WU-13...)
            // i dijalektalni nazivi
            "junior", "kadet", "pionir", "limac", "pocetni", "mladez", "skolica",
            "tici", "papaline", "karlici", "zagici", """\bprst""", "spiget",
            """\bw?u ?-? ?\d{1,2}\b""",
            // veterani (uklj. skraćeno "VET")
            """\bvet""",
            // mali nogomet / futsal
            "futsal", "hmnl", "hmnk", "shmnl", "malonogomet", "mali nogomet", "malom nogometu",
            """\bmznl\b""", """\bzmnl\b""", """\bzfl\b""",
            // kupovi (uklj. spojene: mnkup, senkup, superkup — ali NE "skupina"),
            // kvalifikacije, prednatjecanja, doigravanja, turniri
            """(?<!s)kup""", """\bcup\b""", """\bhnk\b""", "kvalifikacij", "prednatjecatelj",
            "doigravanje", "zavrsnica", "prijateljsk", "turnir",
        ).joinToString("|"),
    )

    fun jeSeniorskaLiga(nazivNatjecanja: String): Boolean =
        !ISKLJUCI.containsMatchIn(normalizirajZaPretragu(nazivNatjecanja))
}
