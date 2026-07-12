package com.navijacisazabranom.app.notifikacije

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * Provjere i intenti za pouzdan rad podsjetnika u pozadini: izuzece od
 * optimizacije baterije (standardni Android) i MIUI-jev ekran za autostart
 * (nema API-ja, moze se samo otvoriti ekran Security Centera).
 */
object PouzdanostPomocnik {

    fun izuzetOdOptimizacijeBaterije(context: Context): Boolean =
        context.getSystemService(PowerManager::class.java)
            ?.isIgnoringBatteryOptimizations(context.packageName) == true

    fun zahtjevIzuzecaBaterijeIntent(context: Context): Intent = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}"),
    )

    fun miuiAutostartIntent(): Intent = Intent().setComponent(
        ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity",
        ),
    )

    fun imaMiuiAutostartEkran(context: Context): Boolean =
        context.packageManager.resolveActivity(miuiAutostartIntent(), 0) != null
}
