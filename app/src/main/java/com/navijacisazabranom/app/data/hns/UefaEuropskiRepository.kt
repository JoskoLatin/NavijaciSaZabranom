package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.jsoup.Jsoup
import javax.inject.Inject

class UefaEuropskiRepository @Inject constructor() : EuropskiRepository {

    override suspend fun getEuropskeUtakmice(klubNaziv: String, klubId: String): Result<List<Utakmica>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sezona = HnsConfig.uefaSezonaGodina()
                val sve = mutableListOf<Utakmica>()
                for ((compId, naziv) in HnsConfig.UEFA_NATJECANJA) {
                    sve += dohvatiNatjecanje(compId, sezona, naziv, klubNaziv, klubId)
                }
                sve.distinctBy { it.id }.sortedWith(compareBy({ it.datum }, { it.vrijeme }))
            }
        }

    private fun dohvatiNatjecanje(
        compId: String,
        sezona: Int,
        naziv: String,
        klubNaziv: String,
        klubId: String,
    ): List<Utakmica> {
        val rezultat = mutableListOf<Utakmica>()
        var offset = 0
        while (true) {
            val url = "${HnsConfig.UEFA_MATCHES_URL}?competitionId=$compId&seasonYear=$sezona" +
                "&phase=ALL&offset=$offset&limit=$LIMIT"
            val body = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent(HnsConfig.USER_AGENT)
                .timeout(HnsConfig.CONNECT_TIMEOUT_MS)
                .maxBodySize(0)
                .execute()
                .body()

            val arr = JSONArray(body)
            rezultat += UefaMatchParser.parse(arr, naziv, klubNaziv, klubId)
            if (arr.length() < LIMIT) break
            offset += LIMIT
        }
        return rezultat
    }

    private companion object {
        const val LIMIT = 100
    }
}
