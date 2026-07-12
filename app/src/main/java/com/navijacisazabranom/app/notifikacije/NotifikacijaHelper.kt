package com.navijacisazabranom.app.notifikacije

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Utakmica
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class NotifikacijaHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun kreirajKanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val kanal = NotificationChannel(
            KANAL_ID,
            context.getString(R.string.notifikacija_kanal_naziv),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notifikacija_kanal_opis)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(kanal)
    }

    /** Obavijest o objavi ili promjeni termina; jedna notifikacija i za više promjena odjednom. */
    fun prikaziPromjeneTermina(promjene: List<Utakmica>) {
        if (promjene.isEmpty()) return

        val naslov: String
        val tekst: String
        if (promjene.size == 1) {
            val utakmica = promjene.single()
            val vrijemeText = utakmica.vrijeme?.format(timeFormatter)
                ?: context.getString(R.string.raspored_satnica_tbd)
            naslov = context.getString(R.string.notifikacija_termin_naslov)
            tekst = context.getString(
                R.string.notifikacija_termin_tekst,
                utakmica.domacinNaziv,
                utakmica.gostNaziv,
                utakmica.datum.format(dateFormatter),
                vrijemeText,
            )
        } else {
            naslov = context.getString(R.string.notifikacija_termin_vise_naslov)
            tekst = context.resources.getQuantityString(
                R.plurals.notifikacija_termin_vise_tekst,
                promjene.size,
                promjene.size,
            )
        }
        prikazi(ID_PROMJENA_TERMINA, naslov, tekst)
    }

    fun prikazi(id: Int, naslov: String, tekst: String) {
        val imaDozvolu = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!imaDozvolu) return

        val notifikacija = NotificationCompat.Builder(context, KANAL_ID)
            .setSmallIcon(R.drawable.ic_notifikacija)
            .setContentTitle(naslov)
            .setContentText(tekst)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tekst))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notifikacija)
    }

    companion object {
        const val KANAL_ID = "podsjetnici_javljanje"
        const val ID_PROMJENA_TERMINA = 2001

        private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
