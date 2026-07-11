package com.navijacisazabranom.app.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser>
}
