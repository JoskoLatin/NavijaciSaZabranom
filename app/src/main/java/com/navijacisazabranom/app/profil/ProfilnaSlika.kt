package com.navijacisazabranom.app.profil

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Profilna slika se kopira u internu memoriju aplikacije — URI iz biranja slike
 * vrijedi samo kratko, pa se na njega ne možemo osloniti kasnije.
 */
object ProfilnaSlika {

    private const val DATOTEKA = "profilna.jpg"

    fun datoteka(context: Context): File = File(context.filesDir, DATOTEKA)

    fun postoji(context: Context): Boolean = datoteka(context).exists()

    fun spremi(context: Context, uri: Uri): Result<Unit> = runCatching {
        context.contentResolver.openInputStream(uri).use { ulaz ->
            requireNotNull(ulaz) { "Slika se ne može otvoriti" }
            datoteka(context).outputStream().use { izlaz -> ulaz.copyTo(izlaz) }
        }
    }

    fun obrisi(context: Context) {
        datoteka(context).delete()
    }
}
