package com.navijacisazabranom.app.ui.screens.profil

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.navijacisazabranom.app.R

@Composable
fun UputeTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.upute_naslov),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )

        Odjeljak(R.string.upute_svrha_naslov, R.string.upute_svrha_tekst)
        Odjeljak(R.string.upute_zasto_naslov, R.string.upute_zasto_tekst)
        Odjeljak(R.string.upute_kako_naslov, R.string.upute_kako_tekst)
        Odjeljak(R.string.upute_sat_naslov, R.string.upute_sat_tekst)
        Odjeljak(R.string.upute_podsjetnik_naslov, R.string.upute_podsjetnik_tekst)
        Odjeljak(R.string.upute_novi_naslov, R.string.upute_novi_tekst)
        Odjeljak(R.string.upute_klub_naslov, R.string.upute_klub_tekst)
        Odjeljak(R.string.upute_pouzdanost_naslov, R.string.upute_pouzdanost_tekst)
        Odjeljak(R.string.upute_privatnost_naslov, R.string.upute_privatnost_tekst)
        Odjeljak(R.string.upute_odgovornost_naslov, R.string.upute_odgovornost_tekst)
    }
}

/** Svaka uputa je zasebna kartica — lakše se prelijeće pogledom nego neprekinuti tekst. */
@Composable
private fun Odjeljak(@StringRes naslov: Int, @StringRes tekst: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(naslov),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(tekst),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
