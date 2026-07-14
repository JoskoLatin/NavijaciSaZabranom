package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class HnsReprezentacijaRepository @Inject constructor() : ReprezentacijaRepository {

    override suspend fun getRaspored(): Result<List<Utakmica>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val document = Jsoup.connect(HnsConfig.REPREZENTACIJA_URL)
                    .data("cid", HnsConfig.REPREZENTACIJA_CID)
                    .userAgent(HnsConfig.USER_AGENT)
                    .timeout(HnsConfig.CONNECT_TIMEOUT_MS)
                    .get()

                HnsMatchParser.parseReprezentacija(document)
                    // Kronološki; odigrane (u prošlosti) i nadolazeće u jednoj listi.
                    .sortedWith(compareBy({ it.datum }, { it.vrijeme }))
            }
        }
}
