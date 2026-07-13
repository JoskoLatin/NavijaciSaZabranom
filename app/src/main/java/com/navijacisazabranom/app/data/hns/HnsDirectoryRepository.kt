package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class HnsDirectoryRepository @Inject constructor() : DirectoryRepository {

    override suspend fun getOrganizacije(sezona: String): Result<List<Organizacija>> = withContext(Dispatchers.IO) {
        runCatching {
            val json = fetchJson("${HnsConfig.BASE_URL}/handlers/getOrganizations/") { connection ->
                connection.data("season", sezona)
            }
            HnsDirectoryParser.parseOrganizacije(json)
        }
    }

    override suspend fun getNatjecanja(organizacijaId: String, sezona: String): Result<List<Natjecanje>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = fetchJson("${HnsConfig.BASE_URL}/handlers/getCompetitions/") { connection ->
                    connection
                        .data("season", sezona)
                        .data("oid", organizacijaId)
                        .data("linkType", "competitions")
                        .data("linkConstructor", "/natjecanja/{cid}/{cname}/")
                }
                HnsDirectoryParser.parseNatjecanja(json)
            }
        }

    private fun fetchJson(url: String, extraParams: (org.jsoup.Connection) -> org.jsoup.Connection): String =
        extraParams(
            Jsoup.connect(url)
                .data("t", System.currentTimeMillis().toString())
                .data("lang", "hr")
                .data("teamch", "Club")
                .ignoreContentType(true)
                .userAgent(HnsConfig.USER_AGENT)
                .timeout(HnsConfig.CONNECT_TIMEOUT_MS),
        ).execute().body()
}
