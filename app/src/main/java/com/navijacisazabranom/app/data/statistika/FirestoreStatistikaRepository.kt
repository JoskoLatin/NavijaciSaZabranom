package com.navijacisazabranom.app.data.statistika

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FirestoreStatistikaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : StatistikaRepository {

    override fun zabiljeziOdabirKluba(klubId: String, klubNaziv: String, natjecanje: String) {
        val uid = auth.currentUser?.uid ?: return
        val zapis = mapOf(
            "uid" to uid,
            "klubId" to klubId,
            "klubNaziv" to klubNaziv,
            "natjecanje" to natjecanje,
            "vrijeme" to FieldValue.serverTimestamp(),
        )
        // Firestore lokalno zapiše i sinkronizira u pozadini; pad se samo logira.
        firestore.collection(KOLEKCIJA).add(zapis)
            .addOnFailureListener { Log.w(TAG, "Zapis statistike nije uspio", it) }
    }

    private companion object {
        const val TAG = "StatistikaRepo"
        const val KOLEKCIJA = "odabiri"
    }
}
