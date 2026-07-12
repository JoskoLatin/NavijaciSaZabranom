package com.navijacisazabranom.app.ui.screens.registracija

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.auth.AuthRepository
import com.navijacisazabranom.app.data.auth.MIN_DULJINA_LOZINKE
import com.navijacisazabranom.app.data.auth.jeIspravanEmail
import com.navijacisazabranom.app.data.auth.mapirajAuthGresku
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistracijaUiState(
    val ucitavanje: Boolean = false,
    @StringRes val greska: Int? = null,
)

@HiltViewModel
class RegistracijaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistracijaUiState())
    val uiState: StateFlow<RegistracijaUiState> = _uiState.asStateFlow()

    fun registriraj(email: String, lozinka: String, potvrda: String, onRegistriran: () -> Unit) {
        val cistiEmail = email.trim()
        val greskaValidacije = when {
            !jeIspravanEmail(cistiEmail) -> R.string.validacija_email
            lozinka.length < MIN_DULJINA_LOZINKE -> R.string.auth_error_slaba_lozinka
            lozinka != potvrda -> R.string.validacija_lozinke_razlicite
            else -> null
        }
        if (greskaValidacije != null) {
            _uiState.value = RegistracijaUiState(greska = greskaValidacije)
            return
        }

        _uiState.value = RegistracijaUiState(ucitavanje = true)
        viewModelScope.launch {
            authRepository.registrirajEmailom(cistiEmail, lozinka)
                .onSuccess { onRegistriran() }
                .onFailure { e ->
                    Log.e(TAG, "Registracija neuspješna", e)
                    _uiState.value = RegistracijaUiState(greska = mapirajAuthGresku(e))
                }
        }
    }

    private companion object {
        const val TAG = "RegistracijaViewModel"
    }
}
