package com.navijacisazabranom.app.ui.screens.potvrdiemail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun PotvrdiEmailScreen(
    onVerificiran: () -> Unit,
    onOdjava: () -> Unit,
    viewModel: PotvrdiEmailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    PotvrdiEmailContent(
        uiState = uiState,
        email = viewModel.email,
        onProvjeri = { viewModel.provjeri(onVerificiran) },
        onPosaljiPonovno = viewModel::posaljiPonovno,
        onOdjava = { viewModel.odjava(onOdjava) },
    )
}

@Composable
private fun PotvrdiEmailContent(
    uiState: PotvrdiEmailUiState,
    email: String,
    onProvjeri: () -> Unit,
    onPosaljiPonovno: () -> Unit,
    onOdjava: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.potvrdi_email_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.potvrdi_email_tekst, email),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        uiState.greska?.let { greska ->
            Text(
                text = stringResource(greska),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        uiState.poruka?.let { poruka ->
            Text(
                text = stringResource(poruka),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        if (uiState.ucitavanje) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onProvjeri, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.potvrdi_email_provjerio))
            }
            TextButton(onClick = onPosaljiPonovno, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = stringResource(R.string.potvrdi_email_ponovno))
            }
            TextButton(onClick = onOdjava) {
                Text(text = stringResource(R.string.action_odjava))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PotvrdiEmailScreenPreview() {
    NavijaciTheme {
        PotvrdiEmailContent(
            uiState = PotvrdiEmailUiState(),
            email = "korisnik@primjer.hr",
            onProvjeri = {},
            onPosaljiPonovno = {},
            onOdjava = {},
        )
    }
}
