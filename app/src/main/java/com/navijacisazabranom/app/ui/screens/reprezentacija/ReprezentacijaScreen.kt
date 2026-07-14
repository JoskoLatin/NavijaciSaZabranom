package com.navijacisazabranom.app.ui.screens.reprezentacija

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.ui.components.CenteredBox
import com.navijacisazabranom.app.ui.theme.NavijaciTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ReprezentacijaScreen(viewModel: ReprezentacijaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    ReprezentacijaContent(uiState = uiState, onRetry = viewModel::osvjezi)
}

@Composable
private fun ReprezentacijaContent(
    uiState: ReprezentacijaUiState,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.reprezentacija_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        when {
            uiState.ucitava -> CenteredBox { CircularProgressIndicator() }
            uiState.greska -> CenteredBox {
                Text(
                    text = stringResource(R.string.reprezentacija_greska),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
            uiState.utakmice.isEmpty() -> CenteredBox {
                Text(
                    text = stringResource(R.string.reprezentacija_prazno),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            else -> LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(uiState.utakmice, key = { it.id }) { utakmica ->
                    ReprezentacijaRow(utakmica)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun ReprezentacijaRow(utakmica: Utakmica) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        val vrijemeText = utakmica.vrijeme?.format(timeFormatter)
            ?: stringResource(R.string.raspored_satnica_tbd)
        val naslov = listOfNotNull(
            "${utakmica.datum.format(dateFormatter)} $vrijemeText",
            utakmica.natjecanje,
        ).joinToString(" · ")
        Text(text = naslov, style = MaterialTheme.typography.labelMedium)

        val rezultat = if (utakmica.rezultatDomacin != null && utakmica.rezultatGost != null) {
            "  ${utakmica.rezultatDomacin}:${utakmica.rezultatGost}"
        } else {
            ""
        }
        Text(
            text = "${utakmica.domacinNaziv} — ${utakmica.gostNaziv}$rezultat",
            style = MaterialTheme.typography.bodyLarge,
        )
        utakmica.stadion?.let { stadion ->
            Text(text = stadion, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReprezentacijaContentPreview() {
    NavijaciTheme {
        ReprezentacijaContent(
            uiState = ReprezentacijaUiState(
                ucitava = false,
                utakmice = listOf(
                    Utakmica(
                        id = "1",
                        kolo = -1,
                        datum = LocalDate.of(2026, 11, 15),
                        vrijeme = null,
                        domacinId = "70000",
                        domacinNaziv = "Hrvatska",
                        gostId = "72199",
                        gostNaziv = "Češka",
                        stadion = null,
                        rezultatDomacin = null,
                        rezultatGost = null,
                        natjecanje = "Liga nacija - Skupina A",
                    ),
                ),
            ),
            onRetry = {},
        )
    }
}
