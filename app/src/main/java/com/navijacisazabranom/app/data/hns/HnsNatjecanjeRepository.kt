package com.navijacisazabranom.app.data.hns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class HnsNatjecanjeRepository @Inject constructor() : NatjecanjeRepository {

    override suspend fun getNatjecanjeStranica(natjecanjeId: String): Result<NatjecanjeStranica> =
        withContext(Dispatchers.IO) {
            runCatching {
                val document = Jsoup.connect("${HnsConfig.BASE_URL}/natjecanja/$natjecanjeId/")
                    .userAgent(HnsConfig.USER_AGENT)
                    .timeout(HnsConfig.CONNECT_TIMEOUT_MS)
                    .get()

                NatjecanjeStranica(
                    klubovi = HnsMatchParser.parseKlubovi(document),
                    utakmice = HnsMatchParser.parseUtakmice(document),
                )
            }
        }
}
