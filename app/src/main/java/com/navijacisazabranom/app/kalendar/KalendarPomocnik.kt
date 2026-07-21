package com.navijacisazabranom.app.kalendar

import android.content.ContentValues
import android.content.Context
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

    /**
     * Upisuje više termina odjednom izravno u kalendar (traži dozvolu WRITE_CALENDAR).
     * Vraća mapu id utakmice → id događaja u kalendaru, da se poslije može provjeriti
     * je li korisnik termin u međuvremenu obrisao.
     */
    fun dodajSve(context: Context, utakmice: List<Utakmica>, opis: String): Result<Map<String, Long>> =
        runCatching {
            val kalendarId = pronadjiZapisiviKalendar(context)
                ?: error("Nije pronađen kalendar u koji se može pisati")

            val zapisi = mutableMapOf<String, Long>()
            utakmice.forEach { utakmica ->
                val vrijednosti = vrijednostiZaUpis(utakmica, kalendarId, opis)
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, vrijednosti)
                uri?.lastPathSegment?.toLongOrNull()?.let { zapisi[utakmica.id] = it }
            }
            zapisi
        }

    /**
     * Od predanih id-jeva događaja vraća one koji još postoje u kalendaru. Korisnik
     * termine može obrisati izravno u kalendaru, pa se na to ne smijemo osloniti na
     * ono što smo mi zabilježili pri upisu.
     */
    fun postojeciTermini(context: Context, idjeviDogadjaja: Set<Long>): Result<Set<Long>> = runCatching {
        if (idjeviDogadjaja.isEmpty()) return@runCatching emptySet()

        // Vrijednosti su Long iz baze pa ih je sigurno ugraditi izravno u uvjet.
        val uvjet = "${CalendarContract.Events._ID} IN (${idjeviDogadjaja.joinToString(",")})" +
            " AND ${CalendarContract.Events.DELETED} = 0"

        val postojeci = mutableSetOf<Long>()
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            uvjet,
            null,
            null,
        )?.use { kursor ->
            while (kursor.moveToNext()) postojeci.add(kursor.getLong(0))
        }
        postojeci
    }

    private fun vrijednostiZaUpis(utakmica: Utakmica, kalendarId: Long, opis: String) =
        ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, kalendarId)
            put(CalendarContract.Events.TITLE, "${utakmica.domacinNaziv} — ${utakmica.gostNaziv}")
            put(CalendarContract.Events.DESCRIPTION, opis)
            utakmica.stadion?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }

            if (utakmica.vrijeme == null) {
                // Cjelodnevni termin mora biti ponoć u UTC-u (zahtjev CalendarContracta).
                val pocetak = utakmica.datum.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                put(CalendarContract.Events.DTSTART, pocetak)
                put(CalendarContract.Events.DTEND, pocetak + TimeUnit.DAYS.toMillis(1))
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
            } else {
                val zona = ZoneId.systemDefault()
                val pocetak = utakmica.datum.atTime(utakmica.vrijeme).atZone(zona).toInstant().toEpochMilli()
                put(CalendarContract.Events.DTSTART, pocetak)
                put(CalendarContract.Events.DTEND, pocetak + TimeUnit.MINUTES.toMillis(TRAJANJE_MINUTA))
                put(CalendarContract.Events.EVENT_TIMEZONE, zona.id)
            }
        }

    /** Prvi vidljivi kalendar u koji korisnik smije pisati; prednost ima primarni. */
    private fun pronadjiZapisiviKalendar(context: Context): Long? {
        val projekcija = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.VISIBLE,
        )
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, projekcija, null, null, null,
        )?.use { kursor ->
            var rezerva: Long? = null
            while (kursor.moveToNext()) {
                val id = kursor.getLong(0)
                val primarni = kursor.getInt(1) == 1
                val razinaPristupa = kursor.getInt(2)
                val vidljiv = kursor.getInt(3) == 1
                val smijePisati = razinaPristupa >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR
                if (smijePisati && vidljiv) {
                    if (primarni) return id
                    if (rezerva == null) rezerva = id
                }
            }
            return rezerva
        }
        return null
    }
}
