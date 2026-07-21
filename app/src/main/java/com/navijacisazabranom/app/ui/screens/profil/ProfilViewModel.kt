package com.navijacisazabranom.app.ui.screens.profil

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.auth.AuthRepository
import com.navijacisazabranom.app.data.hns.PraceniKlub
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import com.navijacisazabranom.app.kalendar.KalendarPomocnik
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class ProfilUiState(
    val ucitava: Boolean = true,
    val klub: PraceniKlub? = null,
    val sljedeca: Utakmica? = null,
    val hnsNaopako: Boolean = false,
    val email: String? = null,
    /** Sve nadolazeće utakmice (domaće + europske), kronološki. */
    val nadolazece: List<Utakmica> = emptyList(),
    val uKalendaru: Set<String> = emptySet(),
    val porukaKalendar: String? = null,
) {
    /** Termini koje korisnik još nije spremio u kalendar (npr. nakon europskog ždrijeba). */
    val noviTermini: Int get() = nadolazece.count { it.id !in uKalendaru }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val praceniKlubRepository: PraceniKlubRepository,
    private val postavkeRepository: PostavkeRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val poruka = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfilUiState> = combine(
        praceniKlubRepository.observePraceniKlub(),
        postavkeRepository.observeHnsNaopako(),
        postavkeRepository.observeUKalendaru(),
        poruka,
    ) { klub, naopako, uKalendaru, poruka -> Ulaz(klub, naopako, uKalendaru, poruka) }
        .flatMapLatest { ulaz ->
            if (ulaz.klub == null) {
                flowOf(osnovnoStanje(ulaz))
            } else {
                praceniKlubRepository.observeUtakmice(ulaz.klub.natjecanjeId, ulaz.klub.klubId)
                    .map { utakmice ->
                        val danas = LocalDate.now()
                        val nadolazece = utakmice
                            .filter { it.datum >= danas }
                            .sortedWith(compareBy({ it.datum }, { it.vrijeme }))
                        osnovnoStanje(ulaz).copy(
                            klub = ulaz.klub,
                            sljedeca = nadolazece.firstOrNull(),
                            nadolazece = nadolazece,
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

    /** Upisuje sve nadolazeće termine koji još nisu u kalendaru (dozvola se traži u UI-ju). */
    fun dodajSezonuUKalendar() {
        viewModelScope.launch {
            val stanje = uiState.value
            val zaDodati = stanje.nadolazece.filter { it.id !in stanje.uKalendaru }
            if (zaDodati.isEmpty()) {
                poruka.value = context.getString(R.string.kalendar_nema_novih)
                return@launch
            }

            withContext(Dispatchers.IO) {
                KalendarPomocnik.dodajSve(context, zaDodati, context.getString(R.string.kalendar_opis))
            }
                .onSuccess { broj ->
                    postavkeRepository.zabiljeziUKalendaru(zaDodati.map { it.id }.toSet())
                    poruka.value = context.getString(R.string.kalendar_dodano, broj)
                }
                .onFailure { poruka.value = context.getString(R.string.kalendar_greska) }
        }
    }

    fun ocistiPoruku() {
        poruka.value = null
    }

    fun preokreniHns() {
        viewModelScope.launch { postavkeRepository.preokreniHnsLogo() }
    }

    fun odjava(onOdjavljen: () -> Unit) {
        authRepository.odjava()
        onOdjavljen()
    }

    private fun osnovnoStanje(ulaz: Ulaz) = ProfilUiState(
        ucitava = false,
        hnsNaopako = ulaz.naopako,
        email = authRepository.currentUser?.email,
        uKalendaru = ulaz.uKalendaru,
        porukaKalendar = ulaz.poruka,
    )

    private data class Ulaz(
        val klub: PraceniKlub?,
        val naopako: Boolean,
        val uKalendaru: Set<String>,
        val poruka: String?,
    )
}
