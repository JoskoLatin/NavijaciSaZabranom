package com.navijacisazabranom.app.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?

    /** Prijavljen email korisnik koji još nije kliknuo poveznicu za potvrdu. */
    val trebaVerifikacijuEmaila: Boolean

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser>

    /** Stvara račun i (best effort) odmah šalje verifikacijski email. */
    suspend fun registrirajEmailom(email: String, lozinka: String): Result<Unit>

    suspend fun prijaviEmailom(email: String, lozinka: String): Result<Unit>

    suspend fun posaljiVerifikacijskiEmail(): Result<Unit>

    /** Osvježava korisnika s poslužitelja i vraća je li email potvrđen. */
    suspend fun provjeriVerifikaciju(): Result<Boolean>

    suspend fun posaljiResetLozinke(email: String): Result<Unit>

    fun odjava()
}
