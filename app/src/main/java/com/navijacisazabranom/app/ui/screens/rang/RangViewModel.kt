package com.navijacisazabranom.app.ui.screens.rang

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.DirectoryRepository
import com.navijacisazabranom.app.data.hns.Natjecanje
import com.navijacisazabranom.app.data.hns.Organizacija
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RangUiState {
    data object Loading : RangUiState
    data class Error(val message: String) : RangUiState
    data class Success(
        val organizacije: List<Organizacija>,
        val odabranaOrganizacija: Organizacija,
        val natjecanja: List<Natjecanje>,
        val ucitavanjeNatjecanja: Boolean,
        val odabranoNatjecanje: Natjecanje?,
    ) : RangUiState
}

@HiltViewModel
class RangViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RangUiState>(RangUiState.Loading)
    val uiState: StateFlow<RangUiState> = _uiState.asStateFlow()

    init {
        ucitajOrganizacije()
    }

    fun ucitajOrganizacije() {
        _uiState.value = RangUiState.Loading
        viewModelScope.launch {
            directoryRepository.getOrganizacije()
                .onSuccess { organizacije ->
                    val zadana = organizacije.firstOrNull { it.id == HNS_ID } ?: organizacije.firstOrNull()
                    if (zadana == null) {
                        _uiState.value = RangUiState.Error(context.getString(R.string.rang_error_generic))
                    } else {
                        ucitajNatjecanja(organizacije, zadana)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Dohvat saveza neuspješan", e)
                    _uiState.value = RangUiState.Error(context.getString(R.string.rang_error_generic))
                }
        }
    }

    fun odaberiOrganizaciju(organizacija: Organizacija) {
        val trenutno = _uiState.value as? RangUiState.Success ?: return
        ucitajNatjecanja(trenutno.organizacije, organizacija)
    }

    fun odaberiNatjecanje(natjecanje: Natjecanje) {
        _uiState.update { state ->
            (state as? RangUiState.Success)?.copy(odabranoNatjecanje = natjecanje) ?: state
        }
    }

    private fun ucitajNatjecanja(organizacije: List<Organizacija>, organizacija: Organizacija) {
        _uiState.value = RangUiState.Success(
            organizacije = organizacije,
            odabranaOrganizacija = organizacija,
            natjecanja = emptyList(),
            ucitavanjeNatjecanja = true,
            odabranoNatjecanje = null,
        )
        viewModelScope.launch {
            directoryRepository.getNatjecanja(organizacija.id)
                .onSuccess { natjecanja ->
                    _uiState.update { state ->
                        (state as? RangUiState.Success)?.copy(
                            natjecanja = natjecanja,
                            ucitavanjeNatjecanja = false,
                            odabranoNatjecanje = natjecanja.firstOrNull(),
                        ) ?: state
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Dohvat natjecanja neuspješan", e)
                    _uiState.update { state ->
                        (state as? RangUiState.Success)?.copy(ucitavanjeNatjecanja = false) ?: state
                    }
                }
        }
    }

    private companion object {
        const val TAG = "RangViewModel"
        const val HNS_ID = "1"
    }
}
