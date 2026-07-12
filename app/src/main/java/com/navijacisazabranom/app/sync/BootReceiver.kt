package com.navijacisazabranom.app.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * AlarmManager alarmi ne prezivljavaju reboot NI update aplikacije. Umjesto
 * duplicirane logike za ponovno zakazivanje, samo pokrenemo isti
 * sinkronizacijski posao koji (kao nuspojavu) vec ponovno zakazuje
 * notifikacije nakon uspjesnog dohvata.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<SinkronizacijaWorker>().build())
    }
}
