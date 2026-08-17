package com.navijacisazabranom.app.ui.screens.profil

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.auth.AuthRepository
import com.navijacisazabranom.app.data.hns.PraceniKlub
import com.navijacisazabranom.app.data.hns.PraceniKlubRepository
import com.navijacisazabranom.app.data.hns.ReprezentacijaRepository
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.data.postavke.PostavkeRepository
import com.navijacisazabranom.app.kalendar.KalendarUpis
import com.navijacisazabranom.app.profil.ProfilnaSlika
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
    /** Nadolazeće utakmice reprezentacije — u kalendar idu skupa s klupskima. */
    val reprezentacija: List<Utakmica> = emptyList(),
    /** Id utakmice → id događaja u kalendaru, za već upisane termine. */
    val uKalendaru: Map<String, Long> = emptyMap(),
    val porukaKalendar: String? = null,
    /** Vrijeme zadnje promjene profilne slike; 0 = nema slike. Mijenja se i radi osvježavanja prikaza. */
    val profilnaAzurirana: Long = 0L,
) {
    /** Termini kojih nema u kalendaru — novi (npr. nakon ždrijeba) ili korisnikom obrisani. */
    val noviTermini: Int get() = nadolazece.count { it.id !in uKalendaru } +
        reprezentacija.count { KalendarUpis.PREFIKS_REPREZENTACIJA + it.id !in uKalendaru }

    /** Ima li uopće što upisati u kalendar (inače kartica nema što ponuditi ni potvrditi). */
    val imaTermina: Boolean get() = nadolazece.isNotEmpty() || reprezentacija.isNotEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val praceniKlubRepository: PraceniKlubRepository,
    private val reprezentacijaRepository: ReprezentacijaRepository,
    private val postavkeRepository: PostavkeRepository,
    private val authRepository: AuthRepository,
    private val kalendarUpis: KalendarUpis,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val poruka = MutableStateFlow<String?>(null)

    /** Nadolazeće utakmice reprezentacije; prazno dok se ne dohvate (ili ako dohvat padne). */
    private val reprezentacija = MutableStateFlow<List<Utakmica>>(emptyList())

    val uiState: StateFlow<ProfilUiState> = combine(
        praceniKlubRepository.observePraceniKlub(),
        postavkeRepository.observeHnsNaopako(),
        postavkeRepository.observeUKalendaru(),
        poruka,
        postavkeRepository.observeProfilnaAzurirana(),
    ) { klub, naopako, uKalendaru, poruka, profilna ->
        Ulaz(klub, naopako, uKalendaru, poruka, profilna)
    }
        .combine(reprezentacija) { ulaz, repre -> ulaz.copy(reprezentacija = repre) }
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
        // Reprezentacija se ne sprema u bazu; dohvat je neobavezan — ako padne, ostaje samo klupski raspored.
        viewModelScope.launch {
            reprezentacijaRepository.getRaspored().onSuccess { utakmice ->
                val danas = LocalDate.now()
                reprezentacija.value = utakmice.filter { it.datum >= danas }
            }
        }
        provjeriKalendar()
    }

    /**
     * Briše zapise o terminima kojih više nema u kalendaru (korisnik ih je obrisao ondje),
     * da ih aplikacija ponovno ponudi za dodavanje umjesto da tvrdi da su već upisani.
     */
    fun provjeriKalendar() {
        viewModelScope.launch { kalendarUpis.ocistiObrisane() }
    }

    /**
     * Upisuje sve nadolazeće termine koji još nisu u kalendaru — klupske i reprezentativne
     * zajedno, jednim potezom (dozvola se traži u UI-ju).
     */
    fun dodajSezonuUKalendar() {
        viewModelScope.launch {
            val stanje = uiState.value
            val klupski = kalendarUpis.dodajNove(
                utakmice = stanje.nadolazece,
                opis = context.getString(R.string.kalendar_opis),
            )
            val repre = kalendarUpis.dodajNove(
                utakmice = stanje.reprezentacija,
                opis = context.getString(R.string.reprezentacija_kalendar_stavka_opis),
                prefiks = KalendarUpis.PREFIKS_REPREZENTACIJA,
            )

            val dodano = klupski.getOrDefault(0) + repre.getOrDefault(0)
            poruka.value = when {
                dodano > 0 -> context.getString(R.string.kalendar_dodano, dodano)
                klupski.isFailure || repre.isFailure -> context.getString(R.string.kalendar_greska)
                else -> context.getString(R.string.kalendar_nema_novih)
            }
        }
    }

    fun ocistiPoruku() {
        poruka.value = null
    }

    fun preokreniHns() {
        viewModelScope.launch { postavkeRepository.preokreniHnsLogo() }
    }

    /** Kopira odabranu sliku u internu memoriju; vrijeme promjene osvježava prikaz. */
    fun postaviProfilnuSliku(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { ProfilnaSlika.spremi(context, uri) }
                .onSuccess { postavkeRepository.postaviProfilnaAzurirana(System.currentTimeMillis()) }
                .onFailure { poruka.value = context.getString(R.string.profilna_greska) }
        }
    }

    fun odjava(onOdjavljen: () -> Unit) {
        authRepository.odjava()
        onOdjavljen()
    }

    private fun osnovnoStanje(ulaz: Ulaz) = ProfilUiState(
        ucitava = false,
        hnsNaopako = ulaz.naopako,
        email = authRepository.currentUser?.email,
        reprezentacija = ulaz.reprezentacija,
        uKalendaru = ulaz.uKalendaru,
        porukaKalendar = ulaz.poruka,
        profilnaAzurirana = ulaz.profilnaAzurirana,
    )

    private data class Ulaz(
        val klub: PraceniKlub?,
        val naopako: Boolean,
        val uKalendaru: Map<String, Long>,
        val poruka: String?,
        val profilnaAzurirana: Long,
        val reprezentacija: List<Utakmica> = emptyList(),
    )
}
