package com.navijacisazabranom.app.ui.screens.klub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Klub
import com.navijacisazabranom.app.ui.components.CenteredBox
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun KlubScreen(onKlubSelected: (String) -> Unit, viewModel: KlubViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    KlubContent(
        uiState = uiState,
        query = query,
        onQueryChange = { query = it },
        onKlubSelected = onKlubSelected,
        onRetry = viewModel::ucitajKlubove,
    )
}

@Composable
private fun KlubContent(
    uiState: KlubUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onKlubSelected: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.klub_title), style = MaterialTheme.typography.headlineSmall)

        when (uiState) {
            is KlubUiState.Loading -> CenteredBox { CircularProgressIndicator() }
            is KlubUiState.Error -> CenteredBox {
                Text(
                    text = uiState.message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
            is KlubUiState.Success -> {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text(text = stringResource(R.string.klub_search_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )

                val filtrirano = remember(query, uiState.klubovi) {
                    uiState.klubovi.filter { it.naziv.contains(query, ignoreCase = true) }
                }

                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(filtrirano, key = { it.id }) { klub ->
                        ListItem(
                            headlineContent = { Text(text = klub.naziv) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onKlubSelected(klub.id) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KlubScreenPreview() {
    NavijaciTheme {
        KlubContent(
            uiState = KlubUiState.Success(listOf(Klub("515", "HNK Hajduk"))),
            query = "",
            onQueryChange = {},
            onKlubSelected = {},
            onRetry = {},
        )
    }
}
