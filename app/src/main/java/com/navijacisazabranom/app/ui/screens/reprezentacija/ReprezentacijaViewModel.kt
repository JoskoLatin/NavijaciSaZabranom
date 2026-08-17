package com.navijacisazabranom.app.ui.screens.reprezentacija

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.data.hns.ReprezentacijaRepository
import com.navijacisazabranom.app.data.hns.Utakmica
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReprezentacijaUiState(
    val ucitava: Boolean = true,
    val utakmice: List<Utakmica> = emptyList(),
    val greska: Boolean = false,
)

/**
 * Ekran je samo pregled rasporeda — upis u kalendar ide skupno s klupskim
 * terminima preko kartice na tabu Klub.
 */
@HiltViewModel
class ReprezentacijaViewModel @Inject constructor(
    private val reprezentacijaRepository: ReprezentacijaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReprezentacijaUiState())
    val uiState: StateFlow<ReprezentacijaUiState> = _uiState.asStateFlow()

    init {
        osvjezi()
    }

    fun osvjezi() {
        _uiState.value = _uiState.value.copy(ucitava = true, greska = false)
        viewModelScope.launch {
            reprezentacijaRepository.getRaspored().fold(
                onSuccess = { _uiState.value = ReprezentacijaUiState(ucitava = false, utakmice = it) },
                onFailure = { _uiState.value = ReprezentacijaUiState(ucitava = false, greska = true) },
            )
        }
    }
}
