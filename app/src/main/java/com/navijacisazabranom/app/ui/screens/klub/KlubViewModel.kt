package com.navijacisazabranom.app.ui.screens.klub

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Klub
import com.navijacisazabranom.app.data.hns.NatjecanjeRepository
import com.navijacisazabranom.app.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface KlubUiState {
    data object Loading : KlubUiState
    data class Error(val message: String) : KlubUiState
    data class Success(val klubovi: List<Klub>) : KlubUiState
}

@HiltViewModel
class KlubViewModel @Inject constructor(
    private val natjecanjeRepository: NatjecanjeRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val natjecanjeId: String = checkNotNull(savedStateHandle[Screen.Klub.ARG_NATJECANJE_ID])

    private val _uiState = MutableStateFlow<KlubUiState>(KlubUiState.Loading)
    val uiState: StateFlow<KlubUiState> = _uiState.asStateFlow()

    init {
        ucitajKlubove()
    }

    fun ucitajKlubove() {
        _uiState.value = KlubUiState.Loading
        viewModelScope.launch {
            natjecanjeRepository.getNatjecanjeStranica(natjecanjeId)
                .onSuccess { stranica -> _uiState.value = KlubUiState.Success(stranica.klubovi) }
                .onFailure { e ->
                    Log.e(TAG, "Dohvat klubova neuspješan", e)
                    _uiState.value = KlubUiState.Error(context.getString(R.string.klub_error_generic))
                }
        }
    }

    private companion object {
        const val TAG = "KlubViewModel"
    }
}
