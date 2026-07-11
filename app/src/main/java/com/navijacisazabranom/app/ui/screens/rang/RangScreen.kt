package com.navijacisazabranom.app.ui.screens.rang

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Natjecanje
import com.navijacisazabranom.app.data.hns.Organizacija
import com.navijacisazabranom.app.ui.components.CenteredBox
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun RangScreen(onNatjecanjeSelected: (String) -> Unit, viewModel: RangViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    RangContent(
        uiState = uiState,
        onOrganizacijaSelected = viewModel::odaberiOrganizaciju,
        onNatjecanjeSelected = viewModel::odaberiNatjecanje,
        onRetry = viewModel::ucitajOrganizacije,
        onNext = { natjecanje -> onNatjecanjeSelected(natjecanje.id) },
    )
}

@Composable
private fun RangContent(
    uiState: RangUiState,
    onOrganizacijaSelected: (Organizacija) -> Unit,
    onNatjecanjeSelected: (Natjecanje) -> Unit,
    onRetry: () -> Unit,
    onNext: (Natjecanje) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.rang_title), style = MaterialTheme.typography.headlineSmall)

        when (uiState) {
            is RangUiState.Loading -> CenteredBox { CircularProgressIndicator() }
            is RangUiState.Error -> CenteredBox {
                Text(
                    text = uiState.message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
            is RangUiState.Success -> {
                OrganizacijaDropdown(
                    organizacije = uiState.organizacije,
                    odabrana = uiState.odabranaOrganizacija,
                    onSelected = onOrganizacijaSelected,
                    modifier = Modifier.padding(top = 16.dp),
                )

                when {
                    uiState.ucitavanjeNatjecanja -> CenteredBox(modifier = Modifier.padding(top = 24.dp)) {
                        CircularProgressIndicator()
                    }
                    uiState.natjecanja.isEmpty() -> Text(
                        text = stringResource(R.string.rang_nema_natjecanja),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    else -> {
                        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                            items(uiState.natjecanja, key = { it.id }) { natjecanje ->
                                NatjecanjeRow(
                                    natjecanje = natjecanje,
                                    odabrano = natjecanje.id == uiState.odabranoNatjecanje?.id,
                                    onClick = { onNatjecanjeSelected(natjecanje) },
                                )
                            }
                        }
                        Button(
                            onClick = { uiState.odabranoNatjecanje?.let(onNext) },
                            enabled = uiState.odabranoNatjecanje != null,
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            Text(text = stringResource(R.string.action_next))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrganizacijaDropdown(
    organizacije: List<Organizacija>,
    odabrana: Organizacija,
    onSelected: (Organizacija) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = odabrana.naziv,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.rang_savez_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            organizacije.forEach { organizacija ->
                DropdownMenuItem(
                    text = { Text(text = organizacija.naziv) },
                    onClick = {
                        onSelected(organizacija)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun NatjecanjeRow(natjecanje: Natjecanje, odabrano: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = odabrano, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = odabrano, onClick = onClick)
        Text(text = natjecanje.naziv, modifier = Modifier.padding(start = 8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun RangScreenPreview() {
    NavijaciTheme {
        RangContent(
            uiState = RangUiState.Success(
                organizacije = listOf(Organizacija("1", "Hrvatski nogometni savez")),
                odabranaOrganizacija = Organizacija("1", "Hrvatski nogometni savez"),
                natjecanja = listOf(Natjecanje("114137140", "SuperSport HNL")),
                ucitavanjeNatjecanja = false,
                odabranoNatjecanje = Natjecanje("114137140", "SuperSport HNL"),
            ),
            onOrganizacijaSelected = {},
            onNatjecanjeSelected = {},
            onRetry = {},
            onNext = {},
        )
    }
}
