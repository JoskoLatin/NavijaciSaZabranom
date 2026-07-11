package com.navijacisazabranom.app.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(context: Context, onSuccess: () -> Unit) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle(context)
                .onSuccess { onSuccess() }
                .onFailure { e ->
                    Log.e(TAG, "Google prijava neuspješna", e)
                    _uiState.value = LoginUiState.Error(context.getString(R.string.login_error_generic))
                }
        }
    }

    private companion object {
        const val TAG = "LoginViewModel"
    }
}
