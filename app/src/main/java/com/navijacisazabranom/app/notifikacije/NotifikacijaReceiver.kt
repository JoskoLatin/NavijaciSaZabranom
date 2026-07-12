package com.navijacisazabranom.app.notifikacije

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotifikacijaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val naslov = intent.getStringExtra(EXTRA_NASLOV) ?: return
        val tekst = intent.getStringExtra(EXTRA_TEKST) ?: return
        val id = intent.getIntExtra(EXTRA_ID, 0)
        NotifikacijaHelper(context).prikazi(id, naslov, tekst)
    }

    companion object {
        const val EXTRA_NASLOV = "naslov"
        const val EXTRA_TEKST = "tekst"
        const val EXTRA_ID = "id"
    }
}
