package com.navijacisazabranom.app.notifikacije

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Utakmica
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Zakazuje jutarnju notifikaciju i (samo ako je korisnik ukljucio postavku)
 * vecernju podsjetnu notifikaciju za sljedecu utakmicu pracenog kluba.
 * AlarmManager ne prezivljava reboot uredjaja - vidi sync/BootReceiver koji
 * ponovno pokrece sinkronizaciju (i time raspored alarma) nakon paljenja.
 */
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun zakaziZaSljedecuUtakmicu(klubNaziv: String, sljedeca: Utakmica?, vecernjiPodsjetnik: Boolean) {
        otkaziSve()
        if (sljedeca == null) return

        val vrijemeText = sljedeca.vrijeme?.toString() ?: context.getString(R.string.raspored_satnica_tbd)

        zakaziAlarm(
            requestCode = REQUEST_CODE_DAN_UTAKMICE,
            vrijemeAlarma = sljedeca.datum.atTime(JUTARNJI_SAT, 0),
            naslov = context.getString(R.string.notifikacija_dan_utakmice_naslov, klubNaziv),
            tekst = context.getString(R.string.notifikacija_dan_utakmice_tekst, vrijemeText),
        )

        if (vecernjiPodsjetnik) {
            zakaziAlarm(
                requestCode = REQUEST_CODE_VECER_PRIJE,
                vrijemeAlarma = sljedeca.datum.minusDays(1).atTime(VECERNJI_SAT, 0),
                naslov = context.getString(R.string.notifikacija_vecer_prije_naslov, klubNaziv),
                tekst = context.getString(R.string.notifikacija_vecer_prije_tekst),
            )
        }
    }

    fun otkaziSve() {
        listOf(REQUEST_CODE_DAN_UTAKMICE, REQUEST_CODE_VECER_PRIJE).forEach { requestCode ->
            alarmManager?.cancel(pendingIntent(requestCode, naslov = "", tekst = ""))
        }
    }

    private fun zakaziAlarm(requestCode: Int, vrijemeAlarma: LocalDateTime, naslov: String, tekst: String) {
        val trigerMillis = vrijemeAlarma.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (trigerMillis <= System.currentTimeMillis()) return

        val alarmManager = alarmManager ?: return
        val intent = pendingIntent(requestCode, naslov, tekst)

        // USE_EXACT_ALARM (API 33+) se dodjeljuje automatski; na API 31-32
        // SCHEDULE_EXACT_ALARM korisnik/sustav moze opozvati, pa provjeravamo.
        val smijeTocno = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        if (smijeTocno) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigerMillis, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigerMillis, intent)
        }
    }

    private fun pendingIntent(requestCode: Int, naslov: String, tekst: String): PendingIntent {
        val intent = Intent(context, NotifikacijaReceiver::class.java).apply {
            putExtra(NotifikacijaReceiver.EXTRA_NASLOV, naslov)
            putExtra(NotifikacijaReceiver.EXTRA_TEKST, tekst)
            putExtra(NotifikacijaReceiver.EXTRA_ID, requestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val JUTARNJI_SAT = 9
        const val VECERNJI_SAT = 20
        const val REQUEST_CODE_DAN_UTAKMICE = 1001
        const val REQUEST_CODE_VECER_PRIJE = 1002
    }
}
