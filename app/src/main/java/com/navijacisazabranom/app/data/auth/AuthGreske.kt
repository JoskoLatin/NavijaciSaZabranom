package com.navijacisazabranom.app.data.auth

import androidx.annotation.StringRes
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.navijacisazabranom.app.R

/**
 * Neutralne hrvatske poruke za Firebase Auth greške. Pogrešna lozinka i
 * nepostojeći korisnik namjerno daju istu poruku (ne otkrivamo postoji li
 * račun). Redoslijed je bitan: WeakPassword nasljeđuje InvalidCredentials.
 */
@StringRes
fun mapirajAuthGresku(e: Throwable): Int = when (e) {
    is FirebaseAuthWeakPasswordException -> R.string.auth_error_slaba_lozinka
    is FirebaseAuthUserCollisionException -> R.string.auth_error_email_zauzet
    is FirebaseAuthInvalidCredentialsException -> R.string.auth_error_krivi_podaci
    is FirebaseAuthInvalidUserException -> R.string.auth_error_krivi_podaci
    is FirebaseTooManyRequestsException -> R.string.auth_error_previse_pokusaja
    is FirebaseNetworkException -> R.string.auth_error_mreza
    else -> R.string.auth_error_generic
}
