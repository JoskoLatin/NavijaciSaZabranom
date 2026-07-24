package com.navijacisazabranom.app.ui.screens.profil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.profil.ProfilnaSlika
import com.navijacisazabranom.app.ui.components.CenteredBox
import androidx.compose.ui.unit.dp

@Composable
fun ProfilScreen(
    onPromijeniKlub: () -> Unit,
    onOtvoriRaspored: (natjecanjeId: String, klubId: String) -> Unit,
    onReprezentacija: () -> Unit,
    onNemaKluba: () -> Unit,
    onOdjava: () -> Unit,
    viewModel: ProfilViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    // Klub je polazni tab jer nosi glavni sadržaj.
    var odabraniIndex by rememberSaveable { mutableStateOf(1) }

    when {
        uiState.ucitava -> CenteredBox { CircularProgressIndicator() }
        uiState.klub == null -> LaunchedEffect(Unit) { onNemaKluba() }
        else -> Scaffold(
            bottomBar = {
                NavigationBar {
                    Tab.values().forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = odabraniIndex == index,
                            onClick = { odabraniIndex = index },
                            icon = { TabIkona(tab) },
                            label = { Text(stringResource(tab.natpis)) },
                        )
                    }
                }
            },
        ) { unutarnjiRazmak ->
            Box(modifier = Modifier.padding(unutarnjiRazmak)) {
                when (Tab.values()[odabraniIndex]) {
                    Tab.PROFIL -> ProfilTab(
                        email = uiState.email,
                        profilnaAzurirana = uiState.profilnaAzurirana,
                        onProfilnaOdabrana = viewModel::postaviProfilnuSliku,
                        onOdjava = { viewModel.odjava(onOdjava) },
                    )

                    Tab.KLUB -> KlubTab(
                        klub = uiState.klub!!,
                        sljedeca = uiState.sljedeca,
                        hnsNaopako = uiState.hnsNaopako,
                        noviTermini = uiState.noviTermini,
                        porukaKalendar = uiState.porukaKalendar,
                        onPromijeniKlub = onPromijeniKlub,
                        onOtvoriRaspored = {
                            onOtvoriRaspored(uiState.klub!!.natjecanjeId, uiState.klub!!.klubId)
                        },
                        onReprezentacija = onReprezentacija,
                        onPreokreniHns = viewModel::preokreniHns,
                        onDodajSezonu = viewModel::dodajSezonuUKalendar,
                        onPorukaPrikazana = viewModel::ocistiPoruku,
                        onProvjeriKalendar = viewModel::provjeriKalendar,
                    )

                    Tab.UPUTE -> UputeTab()
                }
            }
        }
    }
}

@Composable
private fun ProfilTab(
    email: String?,
    profilnaAzurirana: Long,
    onProfilnaOdabrana: (Uri) -> Unit,
    onOdjava: () -> Unit,
) {
    val context = LocalContext.current
    // Sistemski birač slika — ne treba dozvola za pristup galeriji.
    val biracSlike = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(onProfilnaOdabrana) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 32.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    biracSlike.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
        ) {
            if (profilnaAzurirana > 0L) {
                AsyncImage(
                    // Vrijeme promjene ulazi u ključ predmemorije jer je putanja uvijek ista.
                    model = ImageRequest.Builder(context)
                        .data(ProfilnaSlika.datoteka(context))
                        .memoryCacheKey(profilnaAzurirana.toString())
                        .diskCacheKey(profilnaAzurirana.toString())
                        .build(),
                    contentDescription = stringResource(R.string.profilna_promijeni),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profilna_dodaj),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp),
                )
            }
        }
        Text(
            text = stringResource(
                if (profilnaAzurirana > 0L) R.string.profilna_promijeni else R.string.profilna_dodaj,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable {
                    biracSlike.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
        )
        Text(
            text = stringResource(R.string.profil_prijavljeni_kao),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = email ?: "—",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onOdjava,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.action_odjava))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TabIkona(tab: Tab) {
    if (tab == Tab.KLUB) {
        // Nogometna lopta je vlastiti vektor (nema je u osnovnom Material setu).
        Icon(painter = painterResource(R.drawable.ic_lopta), contentDescription = null)
    } else {
        Icon(
            imageVector = if (tab == Tab.PROFIL) Icons.Default.Person else Icons.Default.Info,
            contentDescription = null,
        )
    }
}

private enum class Tab(val natpis: Int) {
    PROFIL(R.string.tab_profil),
    KLUB(R.string.tab_klub),
    UPUTE(R.string.tab_upute),
}
