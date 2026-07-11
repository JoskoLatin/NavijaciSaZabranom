package com.navijacisazabranom.app.ui.screens.raspored

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.NatjecanjeRepository
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RasporedUiState {
    data object Loading : RasporedUiState
    data class Success(val utakmice: List<Utakmica>) : RasporedUiState
    data class Error(val message: String) : RasporedUiState
}

@HiltViewModel
class RasporedViewModel @Inject constructor(
    private val natjecanjeRepository: NatjecanjeRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val natjecanjeId: String = checkNotNull(savedStateHandle[Screen.Raspored.ARG_NATJECANJE_ID])
    private val klubId: String = checkNotNull(savedStateHandle[Screen.Raspored.ARG_KLUB_ID])

    private val _uiState = MutableStateFlow<RasporedUiState>(RasporedUiState.Loading)
    val uiState: StateFlow<RasporedUiState> = _uiState.asStateFlow()

    init {
        loadMatches()
    }

    fun loadMatches() {
        _uiState.value = RasporedUiState.Loading
        viewModelScope.launch {
            natjecanjeRepository.getNatjecanjeStranica(natjecanjeId)
                .onSuccess { stranica ->
                    val utakmiceKluba = stranica.utakmice
                        .filter { it.domacinId == klubId || it.gostId == klubId }
                        .sortedBy { it.datum }
                    _uiState.value = RasporedUiState.Success(utakmiceKluba)
                }
                .onFailure { e ->
                    Log.e(TAG, "Dohvat rasporeda neuspješan", e)
                    _uiState.value = RasporedUiState.Error(context.getString(R.string.raspored_error_generic))
                }
        }
    }

    private companion object {
        const val TAG = "RasporedViewModel"
    }
}
