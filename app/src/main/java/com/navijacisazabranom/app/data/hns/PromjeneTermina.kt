package com.navijacisazabranom.app.data.hns

import com.navijacisazabranom.app.data.hns.local.UtakmicaEntity
import java.time.LocalDate

/**
 * Vraća utakmice iz novog dohvata kojima se termin (datum ili satnica)
 * razlikuje od keširanog stanja — uključujući objavu satnice (TBD → poznato).
 * Prazan stari cache znači prvi dohvat: sve je "novo" pa nema promjena o
 * kojima bi se obavještavalo. Prošle utakmice i utakmice kojih nema u starom
 * cacheu (npr. drugih klubova) se preskaču.
 */
internal fun pronadjiPromjeneTermina(
    stare: List<UtakmicaEntity>,
    nove: List<UtakmicaEntity>,
    danas: LocalDate,
): List<UtakmicaEntity> {
    if (stare.isEmpty()) return emptyList()

    val stareMapa = stare.associateBy { it.id }
    return nove.filter { nova ->
        val stara = stareMapa[nova.id] ?: return@filter false
        nova.datum >= danas && (nova.datum != stara.datum || nova.vrijeme != stara.vrijeme)
    }
}
