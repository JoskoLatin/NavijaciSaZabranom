package com.navijacisazabranom.app.data.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser

/**
 * Google računi su uvijek verificirani; blokada vrijedi samo za račune
 * stvorene emailom i lozinkom koji još nisu kliknuli poveznicu za potvrdu.
 */
fun FirebaseUser.trebaVerifikacijuEmaila(): Boolean =
    providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID } && !isEmailVerified
