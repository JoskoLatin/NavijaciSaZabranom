package com.navijacisazabranom.app.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
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
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onTrebaVerifikacija: () -> Unit,
    onRegistracija: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LoginContent(
        uiState = uiState,
        onPrijavaEmailom = { email, lozinka ->
            viewModel.prijavaEmailom(email, lozinka, onLoggedIn, onTrebaVerifikacija)
        },
        onPrijavaGoogle = { viewModel.prijavaGoogle(context, onLoggedIn) },
        onPosaljiReset = viewModel::posaljiResetLozinke,
        onRegistracija = onRegistracija,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onPrijavaEmailom: (String, String) -> Unit,
    onPrijavaGoogle: () -> Unit,
    onPosaljiReset: (String) -> Unit,
    onRegistracija: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var lozinka by remember { mutableStateOf("") }
    var prikaziResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.login_subtitle),
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

        uiState.greska?.let { greska ->
            Text(
                text = stringResource(greska),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        uiState.poruka?.let { poruka ->
            Text(
                text = stringResource(poruka),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (uiState.ucitavanje) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(
                onClick = { onPrijavaEmailom(email, lozinka) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(text = stringResource(R.string.login_prijava_button))
            }
            TextButton(onClick = { prikaziResetDialog = true }) {
                Text(text = stringResource(R.string.login_zaboravljena_lozinka))
            }

            Text(
                text = stringResource(R.string.login_ili),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            OutlinedButton(onClick = onPrijavaGoogle, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.login_google_button))
            }
            TextButton(onClick = onRegistracija, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = stringResource(R.string.login_registracija_link))
            }
        }
    }

    if (prikaziResetDialog) {
        ResetLozinkeDialog(
            pocetniEmail = email,
            onPosalji = { resetEmail ->
                prikaziResetDialog = false
                onPosaljiReset(resetEmail)
            },
            onOdustani = { prikaziResetDialog = false },
        )
    }
}

@Composable
private fun ResetLozinkeDialog(
    pocetniEmail: String,
    onPosalji: (String) -> Unit,
    onOdustani: () -> Unit,
) {
    var resetEmail by remember { mutableStateOf(pocetniEmail) }

    AlertDialog(
        onDismissRequest = onOdustani,
        title = { Text(stringResource(R.string.reset_dialog_naslov)) },
        text = {
            Column {
                Text(stringResource(R.string.reset_dialog_tekst))
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text(stringResource(R.string.login_email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onPosalji(resetEmail) }) {
                Text(stringResource(R.string.action_posalji))
            }
        },
        dismissButton = {
            TextButton(onClick = onOdustani) {
                Text(stringResource(R.string.action_odustani))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NavijaciTheme {
        LoginContent(
            uiState = LoginUiState(),
            onPrijavaEmailom = { _, _ -> },
            onPrijavaGoogle = {},
            onPosaljiReset = {},
            onRegistracija = {},
        )
    }
}
