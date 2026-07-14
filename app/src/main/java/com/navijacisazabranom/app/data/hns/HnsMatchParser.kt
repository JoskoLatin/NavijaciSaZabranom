package com.navijacisazabranom.app.data.hns

import android.util.Log
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime

/**
 * Stranica natjecanja na semafor.hns.family ponavlja isti li.row[data-match]
 * markup i unutar manjih po-klupskih "Utakmice" widgeta (isti data-match ID
 * kao u glavnom rasporedu). Selektor se zato oslanja na
 * div.competition_results_scorers_cards, jedini kontejner cjelovitog
 * sezonskog rasporeda, uz distinctBy(id) kao dodatnu zaštitu. Provjereno i na
 * nižem rangu (županijska liga) - ista klopka i isti markup se ponavljaju.
 */
object HnsMatchParser {

    private const val TAG = "HnsMatchParser"
    private val DATE_REGEX = Regex("""(\d{2})\.(\d{2})\.(\d{4})\.\s*(\d{2}:\d{2})?""")

    /** Klupski raspored: markup se ponavlja u po-klupskim widgetima, pa je nužan uži scope. */
    private const val SCOPE_KLUB = "div.competition_results_scorers_cards li.row[data-match]"

    /**
     * Reprezentacija (rezultati.hns.team): nema competition_results wrappera; redovi su
     * "row visible" i na stranici postoji točno jedan skup pa je scope po klasi dovoljan.
     */
    private const val SCOPE_REPREZENTACIJA = "li.row[data-match]"

    fun parseUtakmice(document: Document): List<Utakmica> =
        parseRedove(document.select(SCOPE_KLUB))

    fun parseReprezentacija(document: Document): List<Utakmica> =
        parseRedove(document.select(SCOPE_REPREZENTACIJA))

    private fun parseRedove(rows: List<Element>): List<Utakmica> =
        rows.mapNotNull { row ->
            runCatching { parseRow(row) }
                .onFailure { Log.w(TAG, "Preskačem redak koji se ne može parsirati: ${it.message}") }
                .getOrNull()
        }.distinctBy { it.id }

    fun parseKlubovi(document: Document): List<Klub> {
        val items = document.select("div.clubs_in_competition ul.club_list_inner li[data-id]")
        return items.mapNotNull { li ->
            val id = li.attr("data-id")
            val naziv = li.selectFirst("div.title h3")?.text().orEmpty()
            val grbUrl = li.selectFirst("div.logo img")
                ?.let { img -> img.absUrl("src").ifBlank { img.attr("src") } }
                ?.takeIf { it.isNotBlank() }
            if (naziv.isBlank()) null else Klub(id, naziv, grbUrl)
        }.distinctBy { it.id }
    }

    private fun parseRow(row: Element): Utakmica {
        val id = row.attr("data-match")
        val kolo = row.attr("data-round").toInt()

        val dateText = row.selectFirst("div.date")?.text().orEmpty()
        val (datum, vrijeme) = parseDatumVrijeme(dateText)

        val club1 = row.selectFirst("div.club1") ?: error("Nedostaje div.club1")
        val club2 = row.selectFirst("div.club2") ?: error("Nedostaje div.club2")

        // Reprezentacijski gost nema <a> (ime je goli tekst), pa fallback na ownText.
        val stadion = (row.selectFirst("div.facility") ?: row.selectFirst("div.stadium"))
            ?.text()?.trim()?.takeIf { it.isNotBlank() && it != "-" }

        return Utakmica(
            id = id,
            kolo = kolo,
            datum = datum,
            vrijeme = vrijeme,
            domacinId = club1.attr("data-id"),
            domacinNaziv = imeKluba(club1),
            gostId = club2.attr("data-id"),
            gostNaziv = imeKluba(club2),
            stadion = stadion,
            rezultatDomacin = row.selectFirst("div.res1")?.text()?.trim()?.toIntOrNull(),
            rezultatGost = row.selectFirst("div.res2")?.text()?.trim()?.toIntOrNull(),
            natjecanje = row.selectFirst("div.competition")?.text()?.trim()
                ?.takeIf { it.isNotBlank() && it != "-" },
        )
    }

    private fun imeKluba(club: Element): String =
        club.selectFirst("a")?.text()?.ifBlank { null } ?: club.ownText().trim()

    private fun parseDatumVrijeme(text: String): Pair<LocalDate, LocalTime?> {
        val match = DATE_REGEX.find(text.trim()) ?: error("Neočekivan format datuma: '$text'")
        val (day, month, year, time) = match.destructured
        val datum = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        val vrijeme = if (time.isBlank()) null else LocalTime.parse(time)
        return datum to vrijeme
    }
}
