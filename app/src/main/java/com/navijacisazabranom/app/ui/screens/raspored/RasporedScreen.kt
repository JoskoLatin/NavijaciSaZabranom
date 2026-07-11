package com.navijacisazabranom.app.ui.screens.raspored

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
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun RasporedScreen(viewModel: RasporedViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    RasporedContent(uiState = uiState, onRetry = viewModel::loadMatches)
}

@Composable
private fun RasporedContent(uiState: RasporedUiState, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.raspored_title), style = MaterialTheme.typography.headlineSmall)

        when (uiState) {
            is RasporedUiState.Loading -> CenteredBox { CircularProgressIndicator() }
            is RasporedUiState.Error -> CenteredBox {
                Text(
                    text = uiState.message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
            is RasporedUiState.Success -> {
                if (uiState.utakmice.isEmpty()) {
                    Text(
                        text = stringResource(R.string.raspored_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                        items(uiState.utakmice, key = { it.id }) { utakmica ->
                            UtakmicaRow(utakmica)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UtakmicaRow(utakmica: Utakmica) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        val vrijemeText = utakmica.vrijeme?.format(timeFormatter) ?: stringResource(R.string.raspored_satnica_tbd)
        Text(
            text = "${utakmica.kolo}. kolo · ${utakmica.datum.format(dateFormatter)} $vrijemeText",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = "${utakmica.domacinNaziv} — ${utakmica.gostNaziv}",
            style = MaterialTheme.typography.bodyLarge,
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
        RasporedContent(uiState = RasporedUiState.Loading, onRetry = {})
    }
}
