package com.navijacisazabranom.app.ui.screens.profil

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.upute_naslov),
            style = MaterialTheme.typography.headlineSmall,
        )

        Odjeljak(R.string.upute_zasto_naslov, R.string.upute_zasto_tekst)
        Odjeljak(R.string.upute_kako_naslov, R.string.upute_kako_tekst)
        Odjeljak(R.string.upute_podsjetnik_naslov, R.string.upute_podsjetnik_tekst)
        Odjeljak(R.string.upute_novi_naslov, R.string.upute_novi_tekst)
        Odjeljak(R.string.upute_pouzdanost_naslov, R.string.upute_pouzdanost_tekst)
        Odjeljak(R.string.upute_svrha_naslov, R.string.upute_svrha_tekst)
    }
}

@Composable
private fun Odjeljak(@StringRes naslov: Int, @StringRes tekst: Int) {
    Text(
        text = stringResource(naslov),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(
        text = stringResource(tekst),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 6.dp),
    )
}
