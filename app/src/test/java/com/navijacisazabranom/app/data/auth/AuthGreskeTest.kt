package com.navijacisazabranom.app.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.navijacisazabranom.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthGreskeTest {

    @Test
    fun `slaba lozinka ima vlastitu poruku iako nasljedjuje invalid credentials`() {
        val e = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "weak", "kratka")

        assertEquals(R.string.auth_error_slaba_lozinka, mapirajAuthGresku(e))
    }

    @Test
    fun `zauzet email`() {
        val e = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "in use")

        assertEquals(R.string.auth_error_email_zauzet, mapirajAuthGresku(e))
    }

    @Test
    fun `pogresna lozinka i nepostojeci korisnik daju istu neutralnu poruku`() {
        val krivaLozinka = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "wrong")
        val nemaKorisnika = FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "missing")

        assertEquals(R.string.auth_error_krivi_podaci, mapirajAuthGresku(krivaLozinka))
        assertEquals(R.string.auth_error_krivi_podaci, mapirajAuthGresku(nemaKorisnika))
    }

    @Test
    fun `previse pokusaja`() {
        assertEquals(
            R.string.auth_error_previse_pokusaja,
            mapirajAuthGresku(FirebaseTooManyRequestsException("blocked")),
        )
    }

    @Test
    fun `mrezna greska`() {
        assertEquals(R.string.auth_error_mreza, mapirajAuthGresku(FirebaseNetworkException("offline")))
    }

    @Test
    fun `nepoznata greska daje genericku poruku`() {
        assertEquals(R.string.auth_error_generic, mapirajAuthGresku(IllegalStateException("x")))
    }
}
