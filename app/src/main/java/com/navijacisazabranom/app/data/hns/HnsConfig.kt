package com.navijacisazabranom.app.data.hns

import java.time.LocalDate

internal object HnsConfig {
    const val BASE_URL = "https://semafor.hns.family"
    const val USER_AGENT = "Mozilla/5.0 (Android) NavijaciSaZabranom/1.0"
    const val CONNECT_TIMEOUT_MS = 15_000

    /** HNS sezona ide otprilike kolovoz-lipanj; nova sezona se objavljuje na Semaforu već u srpnju. */
    fun tekucaSezona(danas: LocalDate = LocalDate.now()): String {
        val pocetnaGodina = if (danas.monthValue >= 7) danas.year else danas.year - 1
        return "$pocetnaGodina/${pocetnaGodina + 1}"
    }
}
