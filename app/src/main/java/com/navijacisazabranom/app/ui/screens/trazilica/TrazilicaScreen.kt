package com.navijacisazabranom.app.ui.screens.trazilica

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.KlubIndeks
import com.navijacisazabranom.app.ui.components.CenteredBox
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun TrazilicaScreen(
    onKlubOdabran: (natjecanjeId: String, klubId: String) -> Unit,
    viewModel: TrazilicaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    TrazilicaContent(
        uiState = uiState,
        onUpit = viewModel::promijeniUpit,
        onKlub = { klub -> viewModel.odaberiKlub(klub, onKlubOdabran) },
    )
}

@Composable
private fun TrazilicaContent(
    uiState: TrazilicaUiState,
    onUpit: (String) -> Unit,
    onKlub: (KlubIndeks) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.trazilica_title), style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.upit,
            onValueChange = onUpit,
            placeholder = { Text(stringResource(R.string.klub_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )

        // Popis već postoji, ali se u pozadini još dopunjava (kraj prvog preuzimanja) —
        // pretraga radi, ovo je samo suptilna napomena, ne blokira.
        if (uiState.brojKlubova > 0 && uiState.napredak != null) {
            Text(
                text = stringResource(R.string.trazilica_dopunjavanje),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        when {
            // Prvo pokretanje ikad: baza je prazna → jednokratno preuzimanje.
            uiState.brojKlubova == 0 -> CenteredBox {
                val napredak = uiState.napredak
                if (napredak != null && napredak.ukupno > 0) {
                    LinearProgressIndicator(
                        progress = napredak.obradjeno / napredak.ukupno.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Text(
                    text = stringResource(R.string.trazilica_prvo_preuzimanje),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            uiState.upit.isBlank() -> CenteredBox {
                Text(
                    text = stringResource(R.string.trazilica_hint_upisite),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            uiState.rezultati.isEmpty() -> CenteredBox {
                Text(
                    text = stringResource(R.string.trazilica_nema_rezultata, uiState.upit),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            else -> LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(uiState.rezultati, key = { it.klubId }) { klub ->
                    KlubRow(klub = klub, onClick = { onKlub(klub) })
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun KlubRow(klub: KlubIndeks, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        AsyncImage(
            model = klub.grbUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_klub_placeholder),
            error = painterResource(R.drawable.ic_klub_placeholder),
            fallback = painterResource(R.drawable.ic_klub_placeholder),
            modifier = Modifier.size(40.dp),
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = klub.naziv, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = klub.natjecanjeNaziv,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrazilicaScreenPreview() {
    NavijaciTheme {
        TrazilicaContent(
            uiState = TrazilicaUiState(
                upit = "haj",
                rezultati = listOf(
                    KlubIndeks("515", "HNK Hajduk", null, "114137140", "SuperSport HNL"),
                ),
                brojKlubova = 1500,
            ),
            onUpit = {},
            onKlub = {},
        )
    }
}
