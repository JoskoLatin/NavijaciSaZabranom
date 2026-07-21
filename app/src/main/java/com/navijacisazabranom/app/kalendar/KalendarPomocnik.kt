package com.navijacisazabranom.app.kalendar

import android.content.Intent
import android.provider.CalendarContract
import com.navijacisazabranom.app.data.hns.Utakmica
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Sprema termin utakmice u korisnikov kalendar preko ACTION_INSERT — otvara
 * kalendar s popunjenim podacima, korisnik samo potvrdi. Ne treba dozvola za
 * kalendar, a podsjetnik onda drži kalendar (pouzdanije od alarma na uređajima
 * koji agresivno gase pozadinske aplikacije).
 */
object KalendarPomocnik {

    private const val TRAJANJE_MINUTA = 120L

    fun intentZaUtakmicu(utakmica: Utakmica, naslov: String, opis: String): Intent {
        val zona = ZoneId.systemDefault()
        val cijeliDan = utakmica.vrijeme == null
        val pocetak = if (cijeliDan) {
            utakmica.datum.atStartOfDay(zona)
        } else {
            utakmica.datum.atTime(utakmica.vrijeme).atZone(zona)
        }.toInstant().toEpochMilli()

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, naslov)
            putExtra(CalendarContract.Events.DESCRIPTION, opis)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, pocetak)
            putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                pocetak + TimeUnit.MINUTES.toMillis(TRAJANJE_MINUTA),
            )
            // Bez objavljene satnice termin ide kao cjelodnevni.
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, cijeliDan)
            utakmica.stadion?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
        }
    }
}
