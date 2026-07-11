package com.navijacisazabranom.app.ui.screens.klub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun KlubScreen(rangId: String, onKlubSelected: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val klubovi = remember(rangId) { placeholderKlubovi }
    val filtered = remember(query, klubovi) {
        klubovi.filter { it.second.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.klub_title), style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(text = stringResource(R.string.klub_search_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(filtered, key = { it.first }) { (id, naziv) ->
                ListItem(
                    headlineContent = { Text(text = naziv) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onKlubSelected(id) },
                )
            }
        }
    }
}

private val placeholderKlubovi = listOf(
    "609" to "GNK Dinamo",
    "515" to "HNK Hajduk",
    "1471" to "HNK Rijeka",
    "914" to "NK Slaven Belupo",
    "5355" to "NK Varaždin",
)

@Preview(showBackground = true)
@Composable
private fun KlubScreenPreview() {
    NavijaciTheme {
        KlubScreen(rangId = "supersport-hnl", onKlubSelected = {})
    }
}
