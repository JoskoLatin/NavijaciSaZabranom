package com.navijacisazabranom.app.ui.screens.registracija

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun RegistracijaScreen(
    onRegistriran: () -> Unit,
    onNatrag: () -> Unit,
    viewModel: RegistracijaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    RegistracijaContent(
        uiState = uiState,
        onRegistriraj = { email, lozinka, potvrda ->
            viewModel.registriraj(email, lozinka, potvrda, onRegistriran)
        },
        onNatrag = onNatrag,
    )
}

@Composable
private fun RegistracijaContent(
    uiState: RegistracijaUiState,
    onRegistriraj: (String, String, String) -> Unit,
    onNatrag: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var lozinka by remember { mutableStateOf("") }
    var potvrda by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.registracija_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.registracija_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_email_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = lozinka,
            onValueChange = { lozinka = it },
            label = { Text(stringResource(R.string.login_lozinka_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        OutlinedTextField(
            value = potvrda,
            onValueChange = { potvrda = it },
            label = { Text(stringResource(R.string.registracija_potvrda_lozinke_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )

        uiState.greska?.let { greska ->
            Text(
                text = stringResource(greska),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (uiState.ucitavanje) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(
                onClick = { onRegistriraj(email, lozinka, potvrda) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(text = stringResource(R.string.registracija_button))
            }
            TextButton(onClick = onNatrag, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = stringResource(R.string.registracija_natrag))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistracijaScreenPreview() {
    NavijaciTheme {
        RegistracijaContent(uiState = RegistracijaUiState(), onRegistriraj = { _, _, _ -> }, onNatrag = {})
    }
}
