package com.navijacisazabranom.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.navijacisazabranom.app.R
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val credentialManager: CredentialManager,
) : AuthRepository {

    init {
        // Firebaseovi mailovi (verifikacija, reset lozinke) uvijek na hrvatskom.
        // useAppLanguage() bi ih slao na jeziku uređaja, pa je telefon na engleskom
        // dobivao mailove na stranom jeziku iako je aplikacija samo hrvatska.
        firebaseAuth.setLanguageCode("hr")
    }

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override val trebaVerifikacijuEmaila: Boolean
        get() = firebaseAuth.currentUser?.trebaVerifikacijuEmaila() == true

    override suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = credentialManager.getCredential(context, request)
            val credential = response.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(IllegalStateException("Neočekivan tip vjerodajnice: ${credential.type}"))
            }

            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val user = firebaseAuth.signInWithCredential(firebaseCredential).await().user
                ?: return Result.failure(IllegalStateException("Firebase prijava nije vratila korisnika"))

            Result.success(user)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(e)
        }
    }

    override suspend fun registrirajEmailom(email: String, lozinka: String): Result<Unit> =
        runCatching {
            firebaseAuth.createUserWithEmailAndPassword(email, lozinka).await()
        }.map {
            // Slanje verifikacije je best effort: račun je stvoren, a korisnik
            // s ekrana za potvrdu uvijek može zatražiti ponovno slanje.
            posaljiVerifikacijskiEmail().onFailure { e ->
                Log.w(TAG, "Slanje verifikacijskog emaila nije uspjelo", e)
            }
        }

    override suspend fun prijaviEmailom(email: String, lozinka: String): Result<Unit> =
        runCatching { firebaseAuth.signInWithEmailAndPassword(email, lozinka).await() }.map {}

    override suspend fun posaljiVerifikacijskiEmail(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: error("Nema prijavljenog korisnika")
        user.sendEmailVerification().await()
    }

    override suspend fun provjeriVerifikaciju(): Result<Boolean> = runCatching {
        val user = firebaseAuth.currentUser ?: error("Nema prijavljenog korisnika")
        user.reload().await()
        user.isEmailVerified
    }

    override suspend fun posaljiResetLozinke(email: String): Result<Unit> = runCatching {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
        } catch (e: FirebaseAuthInvalidUserException) {
            // Namjerno progutano: ne otkrivamo postoji li račun s tim emailom.
            Log.i(TAG, "Reset zatražen za nepostojeći račun")
        }
    }

    override fun odjava() {
        firebaseAuth.signOut()
    }

    private companion object {
        const val TAG = "AuthRepositoryImpl"
    }
}
