package com.navijacisazabranom.app.data.hns

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Parsira UEFA-in JSON (match.uefa.com) i vraća SAMO utakmice praćenog kluba.
 * Hrvatski klubovi se prepoznaju po countryCode=CRO i podudaranju imena
 * (UEFA "Hajduk Split" ↔ HNS "HNK Hajduk"), a strana našeg kluba dobiva
 * HNS klubId da se utakmice uklope u postojeći klupski upit i notifikacije.
 */
object UefaMatchParser {

    private val PREFIKSI = setOf("hnk", "nk", "gnk", "rnk", "mnk", "nogometni", "klub", "sd")
    private val RAZMAK = Regex("\\s+")

    fun parse(jsonArray: String, natjecanjeNaziv: String, klubNaziv: String, klubId: String): List<Utakmica> =
        parse(JSONArray(jsonArray), natjecanjeNaziv, klubNaziv, klubId)

    fun parse(arr: JSONArray, natjecanjeNaziv: String, klubNaziv: String, klubId: String): List<Utakmica> {
        val rezultat = mutableListOf<Utakmica>()
        for (i in 0 until arr.length()) {
            runCatching { mapiraj(arr.getJSONObject(i), natjecanjeNaziv, klubNaziv, klubId) }
                .getOrNull()?.let { rezultat += it }
        }
        return rezultat
    }

    private fun mapiraj(m: JSONObject, naziv: String, klubNaziv: String, klubId: String): Utakmica? {
        val home = m.getJSONObject("homeTeam")
        val away = m.getJSONObject("awayTeam")
        val nasHome = jeNasKlub(home, klubNaziv)
        val nasAway = jeNasKlub(away, klubNaziv)
        if (!nasHome && !nasAway) return null

        val ko = m.getJSONObject("kickOffTime")
        val (datum, vrijeme) = parseVrijeme(ko.optString("dateTime"), ko.optString("date"))

        val total = m.optJSONObject("score")?.optJSONObject("total")
        val odigrano = m.optString("status") == "FINISHED" &&
            total != null && total.has("home") && total.has("away")

        return Utakmica(
            id = "uefa-${m.optString("id")}",
            kolo = -1,
            datum = datum,
            vrijeme = vrijeme,
            domacinId = if (nasHome) klubId else home.optString("id"),
            domacinNaziv = home.optString("internationalName"),
            gostId = if (nasAway) klubId else away.optString("id"),
            gostNaziv = away.optString("internationalName"),
            stadion = m.optJSONObject("stadium")?.optString("internationalName")?.ifBlank { null },
            rezultatDomacin = if (odigrano) total!!.getInt("home") else null,
            rezultatGost = if (odigrano) total!!.getInt("away") else null,
            natjecanje = naziv,
        )
    }

    private fun parseVrijeme(dateTime: String, dateOnly: String): Pair<LocalDate, LocalTime?> {
        if (dateTime.isNotBlank()) {
            runCatching {
                val z = Instant.parse(dateTime).atZone(ZoneId.systemDefault())
                return z.toLocalDate() to z.toLocalTime()
            }
        }
        return LocalDate.parse(dateOnly) to null
    }

    private fun jeNasKlub(team: JSONObject, klubNaziv: String): Boolean =
        team.optString("countryCode") == "CRO" &&
            imenaPodudaraju(klubNaziv, team.optString("internationalName"))

    /** Podudaranje ako dijele barem jedan značajan token (bez klupskih prefiksa). */
    private fun imenaPodudaraju(hns: String, uefa: String): Boolean {
        val u = jezgra(uefa)
        return jezgra(hns).any { it in u }
    }

    private fun jezgra(naziv: String): Set<String> =
        normalizirajZaPretragu(naziv).split(RAZMAK)
            .filter { it.length >= 3 && it !in PREFIKSI }
            .toSet()
}
