package com.navijacisazabranom.app.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )
        Button(onClick = onLoggedIn) {
            Text(text = stringResource(R.string.login_google_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NavijaciTheme {
        LoginScreen(onLoggedIn = {})
    }
}
