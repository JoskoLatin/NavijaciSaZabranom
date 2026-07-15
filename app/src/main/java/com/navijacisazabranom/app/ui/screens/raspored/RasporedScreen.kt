package com.navijacisazabranom.app.ui.screens.raspored

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.navijacisazabranom.app.R
import androidx.compose.ui.graphics.Color
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.data.hns.jeOdigrana
import com.navijacisazabranom.app.notifikacije.PouzdanostPomocnik
import com.navijacisazabranom.app.ui.components.CenteredBox
import com.navijacisazabranom.app.ui.theme.CrvenaOdigrana
import com.navijacisazabranom.app.ui.theme.NavijaciTheme
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun RasporedScreen(viewModel: RasporedViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dozvolaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            dozvolaLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    RasporedContent(
        uiState = uiState,
        onRetry = viewModel::osvjeziUtakmice,
        onVecernjiPodsjetnik = viewModel::postaviVecernjiPodsjetnik,
        onOdbaciKarticu = viewModel::odbaciKarticuPouzdanosti,
    )
}

@Composable
private fun RasporedContent(
    uiState: RasporedUiState,
    onRetry: () -> Unit,
    onVecernjiPodsjetnik: (Boolean) -> Unit,
    onOdbaciKarticu: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.raspored_title), style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.raspored_vecernji_podsjetnik),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = uiState.vecernjiPodsjetnik,
                onCheckedChange = onVecernjiPodsjetnik,
            )
        }

        if (uiState.prikaziKarticuPouzdanosti) {
            PouzdanostCard(
                prikaziBaterija = uiState.prikaziGumbBaterija,
                prikaziAutostart = uiState.prikaziGumbAutostart,
                onOdbaci = onOdbaciKarticu,
            )
        }

        uiState.greska?.let { greska ->
            Text(
                text = greska,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        when {
            uiState.ucitavanje && uiState.utakmice.isEmpty() -> CenteredBox { CircularProgressIndicator() }
            uiState.utakmice.isEmpty() -> CenteredBox {
                Text(
                    text = stringResource(R.string.raspored_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                if (uiState.greska != null) {
                    Button(onClick = onRetry) {
                        Text(text = stringResource(R.string.action_retry))
                    }
                }
            }
            else -> LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(uiState.utakmice, key = { it.id }) { utakmica ->
                    UtakmicaRow(utakmica)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun PouzdanostCard(
    prikaziBaterija: Boolean,
    prikaziAutostart: Boolean,
    onOdbaci: () -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.pouzdanost_naslov),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.pouzdanost_tekst),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (prikaziBaterija) {
                    Button(onClick = {
                        runCatching {
                            context.startActivity(PouzdanostPomocnik.zahtjevIzuzecaBaterijeIntent(context))
                        }
                    }) {
                        Text(text = stringResource(R.string.pouzdanost_baterija))
                    }
                }
                if (prikaziAutostart) {
                    Button(onClick = {
                        runCatching { context.startActivity(PouzdanostPomocnik.miuiAutostartIntent()) }
                    }) {
                        Text(text = stringResource(R.string.pouzdanost_autostart))
                    }
                }
                TextButton(onClick = onOdbaci) {
                    Text(text = stringResource(R.string.pouzdanost_gotovo))
                }
            }
        }
    }
}

@Composable
private fun UtakmicaRow(utakmica: Utakmica) {
    val odigrana = utakmica.jeOdigrana()
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        val vrijemeText = utakmica.vrijeme?.format(timeFormatter) ?: stringResource(R.string.raspored_satnica_tbd)
        // Europske utakmice nose naziv natjecanja umjesto broja kola.
        val oznaka = utakmica.natjecanje ?: "${utakmica.kolo}. kolo"
        Text(
            text = "$oznaka · ${utakmica.datum.format(dateFormatter)} $vrijemeText",
            style = MaterialTheme.typography.labelMedium,
            color = if (utakmica.natjecanje != null) MaterialTheme.colorScheme.primary else Color.Unspecified,
        )
        val rezultat = if (utakmica.rezultatDomacin != null && utakmica.rezultatGost != null) {
            "  ${utakmica.rezultatDomacin}:${utakmica.rezultatGost}"
        } else {
            ""
        }
        Text(
            text = "${utakmica.domacinNaziv} — ${utakmica.gostNaziv}$rezultat",
            style = MaterialTheme.typography.bodyLarge,
            color = if (odigrana) CrvenaOdigrana else Color.Unspecified,
        )
        utakmica.stadion?.let { stadion ->
            Text(text = stadion, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RasporedScreenPreview() {
    NavijaciTheme {
        RasporedContent(
            uiState = RasporedUiState(ucitavanje = true),
            onRetry = {},
            onVecernjiPodsjetnik = {},
            onOdbaciKarticu = {},
        )
    }
}
