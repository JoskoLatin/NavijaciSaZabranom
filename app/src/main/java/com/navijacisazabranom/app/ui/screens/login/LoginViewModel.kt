package com.navijacisazabranom.app.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.auth.AuthRepository
import com.navijacisazabranom.app.data.auth.jeIspravanEmail
import com.navijacisazabranom.app.data.auth.mapirajAuthGresku
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val ucitavanje: Boolean = false,
    @StringRes val greska: Int? = null,
    @StringRes val poruka: Int? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun prijavaGoogle(context: Context, onPrijavljen: () -> Unit) {
        _uiState.value = LoginUiState(ucitavanje = true)
        viewModelScope.launch {
            authRepository.signInWithGoogle(context)
                .onSuccess { onPrijavljen() }
                .onFailure { e ->
                    Log.e(TAG, "Google prijava neuspješna", e)
                    _uiState.value = LoginUiState(greska = R.string.login_error_generic)
                }
        }
    }

    fun prijavaEmailom(
        email: String,
        lozinka: String,
        onPrijavljen: () -> Unit,
        onTrebaVerifikacija: () -> Unit,
    ) {
        val cistiEmail = email.trim()
        if (!jeIspravanEmail(cistiEmail)) {
            _uiState.value = LoginUiState(greska = R.string.validacija_email)
            return
        }
        if (lozinka.isEmpty()) {
            _uiState.value = LoginUiState(greska = R.string.auth_error_krivi_podaci)
            return
        }

        _uiState.value = LoginUiState(ucitavanje = true)
        viewModelScope.launch {
            authRepository.prijaviEmailom(cistiEmail, lozinka)
                .onSuccess {
                    _uiState.value = LoginUiState()
                    if (authRepository.trebaVerifikacijuEmaila) onTrebaVerifikacija() else onPrijavljen()
                }
                .onFailure { e ->
                    Log.e(TAG, "Email prijava neuspješna", e)
                    _uiState.value = LoginUiState(greska = mapirajAuthGresku(e))
                }
        }
    }

    fun posaljiResetLozinke(email: String) {
        val cistiEmail = email.trim()
        if (!jeIspravanEmail(cistiEmail)) {
            _uiState.value = LoginUiState(greska = R.string.validacija_email)
            return
        }

        _uiState.value = LoginUiState(ucitavanje = true)
        viewModelScope.launch {
            authRepository.posaljiResetLozinke(cistiEmail)
                .onSuccess { _uiState.value = LoginUiState(poruka = R.string.reset_poslan) }
                .onFailure { e ->
                    Log.e(TAG, "Slanje reset emaila neuspješno", e)
                    _uiState.value = LoginUiState(greska = mapirajAuthGresku(e))
                }
        }
    }

    private companion object {
        const val TAG = "LoginViewModel"
    }
}
