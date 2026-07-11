package com.navijacisazabranom.app.data.matches

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
 * sezonskog rasporeda, uz distinctBy(id) kao dodatnu zaštitu.
 */
object HnsMatchParser {

    private const val TAG = "HnsMatchParser"
    private val DATE_REGEX = Regex("""(\d{2})\.(\d{2})\.(\d{4})\.\s*(\d{2}:\d{2})?""")

    fun parse(document: Document): List<Utakmica> {
        val rows = document.select("div.competition_results_scorers_cards li.row[data-match]")
        return rows.mapNotNull { row ->
            runCatching { parseRow(row) }
                .onFailure { Log.w(TAG, "Preskačem redak koji se ne može parsirati: ${it.message}") }
                .getOrNull()
        }.distinctBy { it.id }
    }

    private fun parseRow(row: Element): Utakmica {
        val id = row.attr("data-match")
        val kolo = row.attr("data-round").toInt()

        val dateText = row.selectFirst("div.date")?.text().orEmpty()
        val (datum, vrijeme) = parseDatumVrijeme(dateText)

        val club1 = row.selectFirst("div.club1") ?: error("Nedostaje div.club1")
        val club2 = row.selectFirst("div.club2") ?: error("Nedostaje div.club2")

        return Utakmica(
            id = id,
            kolo = kolo,
            datum = datum,
            vrijeme = vrijeme,
            domacinId = club1.attr("data-id"),
            domacinNaziv = club1.selectFirst("a")?.text().orEmpty(),
            gostId = club2.attr("data-id"),
            gostNaziv = club2.selectFirst("a")?.text().orEmpty(),
            stadion = row.selectFirst("div.facility")?.text()?.ifBlank { null },
            rezultatDomacin = row.selectFirst("div.res1")?.text()?.trim()?.toIntOrNull(),
            rezultatGost = row.selectFirst("div.res2")?.text()?.trim()?.toIntOrNull(),
        )
    }

    private fun parseDatumVrijeme(text: String): Pair<LocalDate, LocalTime?> {
        val match = DATE_REGEX.find(text.trim()) ?: error("Neočekivan format datuma: '$text'")
        val (day, month, year, time) = match.destructured
        val datum = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        val vrijeme = if (time.isBlank()) null else LocalTime.parse(time)
        return datum to vrijeme
    }
}
