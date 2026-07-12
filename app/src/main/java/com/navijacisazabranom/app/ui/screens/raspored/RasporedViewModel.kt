package com.navijacisazabranom.app.ui.screens.raspored

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import com.navijacisazabranom.app.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RasporedUiState(
    val utakmice: List<Utakmica> = emptyList(),
    val ucitavanje: Boolean = true,
    val greska: String? = null,
    val vecernjiPodsjetnik: Boolean = false,
)

@HiltViewModel
class RasporedViewModel @Inject constructor(
    private val praceniKlubRepository: PraceniKlubRepository,
    private val postavkeRepository: PostavkeRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val natjecanjeId: String = checkNotNull(savedStateHandle[Screen.Raspored.ARG_NATJECANJE_ID])
    private val klubId: String = checkNotNull(savedStateHandle[Screen.Raspored.ARG_KLUB_ID])

    private val ucitavanje = MutableStateFlow(true)
    private val greska = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RasporedUiState> = combine(
        praceniKlubRepository.observeUtakmice(natjecanjeId, klubId),
        ucitavanje,
        greska,
        postavkeRepository.observeVecernjiPodsjetnik(),
    ) { utakmice, ucitavanje, greska, vecernjiPodsjetnik ->
        RasporedUiState(
            utakmice = utakmice,
            ucitavanje = ucitavanje,
            greska = greska,
            vecernjiPodsjetnik = vecernjiPodsjetnik,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RasporedUiState())

    init {
        osvjeziUtakmice()
    }

    fun osvjeziUtakmice() {
        ucitavanje.value = true
        greska.value = null
        viewModelScope.launch {
            praceniKlubRepository.osvjeziUtakmice(natjecanjeId)
                .onFailure { e ->
                    Log.e(TAG, "Osvježavanje rasporeda neuspješno", e)
                    greska.value = context.getString(R.string.raspored_error_generic)
                }
            ucitavanje.value = false
        }
    }

    fun postaviVecernjiPodsjetnik(ukljucen: Boolean) {
        viewModelScope.launch {
            postavkeRepository.postaviVecernjiPodsjetnik(ukljucen)
            praceniKlubRepository.ponovnoZakaziNotifikacije()
        }
    }

    private companion object {
        const val TAG = "RasporedViewModel"
    }
}
