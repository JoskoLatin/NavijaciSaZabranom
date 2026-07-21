package com.navijacisazabranom.app.ui.screens.profil

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.PraceniKlub
import com.navijacisazabranom.app.data.hns.Utakmica
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Najviše ovoliko ms između dodira da se broje kao isti niz (triple-tap easter egg). */
private const val TROSTRUKI_PROZOR_MS = 700L

private val DOZVOLE_KALENDAR = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

@Composable
fun KlubTab(
    klub: PraceniKlub,
    sljedeca: Utakmica?,
    hnsNaopako: Boolean,
    noviTermini: Int,
    porukaKalendar: String?,
    onPromijeniKlub: () -> Unit,
    onOtvoriRaspored: () -> Unit,
    onReprezentacija: () -> Unit,
    onPreokreniHns: () -> Unit,
    onDodajSezonu: () -> Unit,
    onPorukaPrikazana: () -> Unit,
) {
    val context = LocalContext.current

    val dozvolaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { rezultati ->
        if (rezultati.values.all { it }) {
            onDodajSezonu()
        } else {
            Toast.makeText(context, R.string.kalendar_dozvola_odbijena, Toast.LENGTH_LONG).show()
        }
    }

    // Rezultat upisa u kalendar javljamo porukom pa je odmah čistimo.
    LaunchedEffect(porukaKalendar) {
        porukaKalendar?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onPorukaPrikazana()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sadržaj kluba skrola samo ako mu zafali mjesta; HNS sekcija ostaje pribijena uz tabove.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = klub.grbUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_klub_placeholder),
                error = painterResource(R.drawable.ic_klub_placeholder),
                fallback = painterResource(R.drawable.ic_klub_placeholder),
                modifier = Modifier.size(80.dp),
            )
            Text(
                text = klub.klubNaziv,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 6.dp),
            )

            OutlinedButton(
                onClick = onPromijeniKlub,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            ) {
                Text(text = stringResource(R.string.profil_promijeni_klub))
            }

            SljedecaUtakmicaKartica(
                sljedeca = sljedeca,
                onClick = onOtvoriRaspored,
                modifier = Modifier.padding(top = 10.dp),
            )

            SezonaUKalendarKartica(
                noviTermini = noviTermini,
                onDodaj = {
                    val imaDozvolu = DOZVOLE_KALENDAR.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (imaDozvolu) onDodajSezonu() else dozvolaLauncher.launch(DOZVOLE_KALENDAR)
                },
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HnsLogo(naopako = hnsNaopako, onPreokreni = onPreokreniHns)
            // Otvaranje rasporeda reprezentacije je na tekstu (logo je rezerviran za easter egg).
            Text(
                text = stringResource(R.string.profil_reprezentacija),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable(onClick = onReprezentacija)
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun SezonaUKalendarKartica(
    noviTermini: Int,
    onDodaj: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.kalendar_sezona_opis),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (noviTermini > 0) {
                Text(
                    text = stringResource(R.string.kalendar_novi_termini, noviTermini),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Button(
                onClick = onDodaj,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(text = stringResource(R.string.kalendar_sezona_gumb))
            }
        }
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
private fun HnsLogo(naopako: Boolean, onPreokreni: () -> Unit) {
    val kut by animateFloatAsState(
        targetValue = if (naopako) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "hns_naopako",
    )
    // Easter egg: tri dodira unutar prozora okrenu logo naopako (i natrag).
    val vremenaDodira = remember { mutableListOf<Long>() }

    AsyncImage(
        model = R.drawable.ic_hns,
        contentDescription = stringResource(R.string.profil_reprezentacija),
        modifier = Modifier
            .size(60.dp)
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
