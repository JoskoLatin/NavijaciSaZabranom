package com.navijacisazabranom.app.ui.screens.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.data.hns.PraceniKlub
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProfilUiState(
    val ucitava: Boolean = true,
    val klub: PraceniKlub? = null,
    val sljedeca: Utakmica? = null,
    val hnsNaopako: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val praceniKlubRepository: PraceniKlubRepository,
    private val postavkeRepository: PostavkeRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfilUiState> = combine(
        praceniKlubRepository.observePraceniKlub(),
        postavkeRepository.observeHnsNaopako(),
    ) { klub, naopako -> klub to naopako }
        .flatMapLatest { (klub, naopako) ->
            if (klub == null) {
                flowOf(ProfilUiState(ucitava = false, klub = null, hnsNaopako = naopako))
            } else {
                praceniKlubRepository.observeUtakmice(klub.natjecanjeId, klub.klubId).map { utakmice ->
                    ProfilUiState(
                        ucitava = false,
                        klub = klub,
                        sljedeca = sljedecaUtakmica(utakmice),
                        hnsNaopako = naopako,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfilUiState())

    init {
        // Profil je početni ekran pa raspored može biti hladan — osvježi da sljedeća utakmica bude točna.
        viewModelScope.launch {
            val klub = praceniKlubRepository.getPraceniKlub() ?: return@launch
            praceniKlubRepository.osvjeziUtakmice(klub.natjecanjeId)
        }
    }

    fun preokreniHns() {
        viewModelScope.launch { postavkeRepository.preokreniHnsLogo() }
    }

    private fun sljedecaUtakmica(utakmice: List<Utakmica>): Utakmica? {
        val danas = LocalDate.now()
        return utakmice.filter { it.datum >= danas }
            .minWithOrNull(compareBy({ it.datum }, { it.vrijeme }))
    }
}
