package com.navijacisazabranom.app.ui.screens.potvrdiemail

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.auth.AuthRepository
import com.navijacisazabranom.app.data.auth.mapirajAuthGresku
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PotvrdiEmailUiState(
    val ucitavanje: Boolean = false,
    @StringRes val greska: Int? = null,
    @StringRes val poruka: Int? = null,
)

@HiltViewModel
class PotvrdiEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PotvrdiEmailUiState())
    val uiState: StateFlow<PotvrdiEmailUiState> = _uiState.asStateFlow()

    val email: String = authRepository.currentUser?.email.orEmpty()

    fun provjeri(onVerificiran: () -> Unit) {
        _uiState.value = PotvrdiEmailUiState(ucitavanje = true)
        viewModelScope.launch {
            authRepository.provjeriVerifikaciju()
                .onSuccess { verificiran ->
                    if (verificiran) {
                        onVerificiran()
                    } else {
                        _uiState.value = PotvrdiEmailUiState(greska = R.string.potvrdi_email_nije_potvrdjen)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Provjera verifikacije neuspješna", e)
                    _uiState.value = PotvrdiEmailUiState(greska = mapirajAuthGresku(e))
                }
        }
    }

    fun posaljiPonovno() {
        _uiState.value = PotvrdiEmailUiState(ucitavanje = true)
        viewModelScope.launch {
            authRepository.posaljiVerifikacijskiEmail()
                .onSuccess { _uiState.value = PotvrdiEmailUiState(poruka = R.string.potvrdi_email_poslan_ponovno) }
                .onFailure { e ->
                    Log.e(TAG, "Ponovno slanje verifikacije neuspješno", e)
                    _uiState.value = PotvrdiEmailUiState(greska = mapirajAuthGresku(e))
                }
        }
    }

    fun odjava(onOdjavljen: () -> Unit) {
        authRepository.odjava()
        onOdjavljen()
    }

    private companion object {
        const val TAG = "PotvrdiEmailViewModel"
    }
}
