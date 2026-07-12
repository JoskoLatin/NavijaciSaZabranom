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

class NotifikacijaHelper(private val context: Context) {

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
    }
}
