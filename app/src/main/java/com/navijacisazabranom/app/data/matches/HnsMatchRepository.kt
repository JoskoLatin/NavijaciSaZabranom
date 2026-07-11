package com.navijacisazabranom.app.data.matches

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class HnsMatchRepository @Inject constructor() : MatchRepository {

    override suspend fun getMatchesForClub(klubId: String): Result<List<Utakmica>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val document = Jsoup.connect(HNL_COMPETITION_URL)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get()

                HnsMatchParser.parse(document)
                    .filter { it.domacinId == klubId || it.gostId == klubId }
                    .sortedBy { it.datum }
            }
        }

    private companion object {
        // SuperSport HNL 2026/27 (ID potvrđen preko /handlers/getCompetitions/); PoC pokriva jedno natjecanje.
        const val HNL_COMPETITION_URL = "https://semafor.hns.family/natjecanja/114137140/supersport-hnl/"
        const val USER_AGENT = "Mozilla/5.0 (Android) NavijaciSaZabranom/1.0"
        const val TIMEOUT_MS = 15_000
    }
}
