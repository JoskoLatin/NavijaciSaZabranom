package com.navijacisazabranom.app.ui.screens.trazilica

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.navijacisazabranom.app.data.hns.IndeksNapredak
import com.navijacisazabranom.app.data.hns.IndeksStanje
import com.navijacisazabranom.app.data.hns.KlubIndeks
import com.navijacisazabranom.app.data.hns.KlubIndeksRepository
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import com.navijacisazabranom.app.sync.IndeksiranjeWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrazilicaUiState(
    val upit: String = "",
    val rezultati: List<KlubIndeks> = emptyList(),
    val brojKlubova: Int = 0,
    val napredak: IndeksNapredak? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrazilicaViewModel @Inject constructor(
    private val klubIndeksRepository: KlubIndeksRepository,
    private val praceniKlubRepository: PraceniKlubRepository,
    indeksStanje: IndeksStanje,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val upit = MutableStateFlow("")

    private val rezultati = upit
        .debounce(200)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else klubIndeksRepository.pretrazi(q)
        }

    val uiState: StateFlow<TrazilicaUiState> = combine(
        upit,
        rezultati,
        klubIndeksRepository.observeBrojKlubova(),
        indeksStanje.napredak,
    ) { upit, rezultati, brojKlubova, napredak ->
        TrazilicaUiState(
            upit = upit,
            rezultati = rezultati,
            brojKlubova = brojKlubova,
            napredak = napredak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrazilicaUiState())

    init {
        // Gradi/osvježava indeks po potrebi; worker sam odustaje ako je svjež.
        IndeksiranjeWorker.pokreni(WorkManager.getInstance(context))
    }

    fun promijeniUpit(novi: String) {
        upit.value = novi
    }

    fun odaberiKlub(klub: KlubIndeks, onOdabran: (natjecanjeId: String, klubId: String) -> Unit) {
        viewModelScope.launch {
            praceniKlubRepository.postaviPraceniKlub(klub.natjecanjeId, klub.klubId, klub.naziv)
            onOdabran(klub.natjecanjeId, klub.klubId)
        }
    }
}
