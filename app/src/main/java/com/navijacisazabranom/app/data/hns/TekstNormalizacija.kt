package com.navijacisazabranom.app.data.hns

import java.text.Normalizer

private val KOMBINIRAJUCI_ZNAKOVI = Regex("\\p{Mn}+")

/**
 * Mala slova bez dijakritika, za usporedbu i LIKE pretragu ("Šibenik" →
 * "sibenik"). NFD razlaže č/ć/š/ž, ali đ je zaseban znak pa se mapira ručno.
 * Podvlaka se tretira kao razmak jer je regex `\b` smatra znakom riječi, pa bi
 * inače "ŽMNL_Veterani" izmaklo filtru granica riječi.
 */
fun normalizirajZaPretragu(tekst: String): String =
    Normalizer.normalize(tekst.lowercase(), Normalizer.Form.NFD)
        .replace(KOMBINIRAJUCI_ZNAKOVI, "")
        .replace('đ', 'd')
        .replace('_', ' ')
