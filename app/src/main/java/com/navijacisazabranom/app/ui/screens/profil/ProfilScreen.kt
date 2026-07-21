package com.navijacisazabranom.app.ui.screens.profil

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.PraceniKlub
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.ui.components.CenteredBox
import com.navijacisazabranom.app.ui.components.DodajUKalendarGumb
import com.navijacisazabranom.app.ui.theme.NavijaciTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Najviše ovoliko ms između dodira da se broje kao isti niz (triple-tap easter egg). */
private const val TROSTRUKI_PROZOR_MS = 700L

@Composable
fun ProfilScreen(
    onPromijeniKlub: () -> Unit,
    onOtvoriRaspored: (natjecanjeId: String, klubId: String) -> Unit,
    onReprezentacija: () -> Unit,
    onNemaKluba: () -> Unit,
    onOdjava: () -> Unit,
    viewModel: ProfilViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.ucitava -> CenteredBox { CircularProgressIndicator() }
        uiState.klub == null -> LaunchedEffect(Unit) { onNemaKluba() }
        else -> ProfilContent(
            klub = uiState.klub!!,
            sljedeca = uiState.sljedeca,
            hnsNaopako = uiState.hnsNaopako,
            onPromijeniKlub = onPromijeniKlub,
            onOtvoriRaspored = { onOtvoriRaspored(uiState.klub!!.natjecanjeId, uiState.klub!!.klubId) },
            onReprezentacija = onReprezentacija,
            onPreokreniHns = viewModel::preokreniHns,
            onOdjava = { viewModel.odjava(onOdjava) },
        )
    }
}

@Composable
private fun ProfilContent(
    klub: PraceniKlub,
    sljedeca: Utakmica?,
    hnsNaopako: Boolean,
    onPromijeniKlub: () -> Unit,
    onOtvoriRaspored: () -> Unit,
    onReprezentacija: () -> Unit,
    onPreokreniHns: () -> Unit,
    onOdjava: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = klub.grbUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_klub_placeholder),
            error = painterResource(R.drawable.ic_klub_placeholder),
            fallback = painterResource(R.drawable.ic_klub_placeholder),
            modifier = Modifier
                .padding(top = 16.dp)
                .size(96.dp),
        )
        Text(
            text = klub.klubNaziv,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp),
        )

        SljedecaUtakmicaKartica(
            sljedeca = sljedeca,
            onClick = onOtvoriRaspored,
            modifier = Modifier.padding(top = 24.dp),
        )

        OutlinedButton(
            onClick = onPromijeniKlub,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text(text = stringResource(R.string.profil_promijeni_klub))
        }

        TextButton(
            onClick = onOdjava,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            Text(text = stringResource(R.string.action_odjava))
        }

        Spacer(modifier = Modifier.weight(1f))

        HnsLogo(
            naopako = hnsNaopako,
            onPreokreni = onPreokreniHns,
        )
        // Otvaranje rasporeda reprezentacije je na tekstu (logo je rezerviran za easter egg).
        Text(
            text = stringResource(R.string.profil_reprezentacija),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable(onClick = onReprezentacija)
                .padding(8.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SljedecaUtakmicaKartica(
    sljedeca: Utakmica?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cijela kartica otvara raspored kluba.
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profil_sljedeca_utakmica),
                style = MaterialTheme.typography.titleSmall,
            )
            if (sljedeca == null) {
                Text(
                    text = stringResource(R.string.profil_nema_sljedece),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                val vrijemeText = sljedeca.vrijeme?.format(timeFormatter)
                    ?: stringResource(R.string.raspored_satnica_tbd)
                Text(
                    text = "${sljedeca.datum.format(dateFormatter)} $vrijemeText",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "${sljedeca.domacinNaziv} — ${sljedeca.gostNaziv}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                sljedeca.stadion?.let { stadion ->
                    Text(text = stadion, style = MaterialTheme.typography.bodySmall)
                }
                // Kalendar drži podsjetnik pouzdanije od alarma na uređajima
                // koji agresivno gase pozadinske aplikacije.
                DodajUKalendarGumb(
                    utakmica = sljedeca,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                text = stringResource(R.string.profil_cijeli_raspored),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HnsLogo(
    naopako: Boolean,
    onPreokreni: () -> Unit,
) {
    val kut by animateFloatAsState(
        targetValue = if (naopako) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "hns_naopako",
    )
    // Easter egg: tri dodira unutar prozora okrenu logo naopako (i natrag). Logo nema
    // drugu akciju pa nema konflikta — detekcija je pouzdana.
    val vremenaDodira = remember { mutableListOf<Long>() }

    AsyncImage(
        model = R.drawable.ic_hns,
        contentDescription = stringResource(R.string.profil_reprezentacija),
        modifier = Modifier
            .size(72.dp)
            .rotate(kut)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    val sada = System.currentTimeMillis()
                    vremenaDodira.add(sada)
                    vremenaDodira.removeAll { sada - it > TROSTRUKI_PROZOR_MS }
                    if (vremenaDodira.size >= 3) {
                        vremenaDodira.clear()
                        onPreokreni()
                    }
                })
            },
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfilContentPreview() {
    NavijaciTheme {
        ProfilContent(
            klub = PraceniKlub("114137140", "515", "HNK Hajduk", null),
            sljedeca = Utakmica(
                id = "1",
                kolo = 1,
                datum = LocalDate.of(2026, 8, 1),
                vrijeme = null,
                domacinId = "5355",
                domacinNaziv = "NK Varaždin",
                gostId = "515",
                gostNaziv = "HNK Hajduk",
                stadion = "Stadion Varteks, Varaždin",
                rezultatDomacin = null,
                rezultatGost = null,
            ),
            hnsNaopako = false,
            onPromijeniKlub = {},
            onOtvoriRaspored = {},
            onReprezentacija = {},
            onPreokreniHns = {},
            onOdjava = {},
        )
    }
}
