package com.navijacisazabranom.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.navijacisazabranom.app.R

private val DOZVOLE_KALENDAR = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

/**
 * Kartica za skupni upis termina u kalendar: traži dozvolu kad treba, a kad su svi
 * termini upisani umjesto gumba prikazuje potvrdu.
 */
@Composable
fun DodajUKalendarKartica(
    opis: String,
    gumbNatpis: String,
    noviTermini: Int,
    imaNadolazecih: Boolean,
    onDodaj: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dozvolaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { rezultati ->
        if (rezultati.values.all { it }) {
            onDodaj()
        } else {
            Toast.makeText(context, R.string.kalendar_dozvola_odbijena, Toast.LENGTH_LONG).show()
        }
    }

    // Kad su svi termini upisani, gumb nema što raditi — zamjenjuje ga potvrda.
    val sveDodano = imaNadolazecih && noviTermini == 0

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = opis, style = MaterialTheme.typography.bodyMedium)

            if (noviTermini > 0) {
                Text(
                    text = stringResource(R.string.kalendar_novi_termini, noviTermini),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (sveDodano) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.kalendar_svi_dodani),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else {
                Button(
                    onClick = {
                        val imaDozvolu = DOZVOLE_KALENDAR.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }
                        if (imaDozvolu) onDodaj() else dozvolaLauncher.launch(DOZVOLE_KALENDAR)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Text(text = gumbNatpis)
                }
            }
        }
    }
}

/** Poruku o rezultatu upisa prikazujemo tostom pa je odmah čistimo. */
@Composable
fun PorukaKalendara(poruka: String?, onPrikazana: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(poruka) {
        poruka?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onPrikazana()
        }
    }
}

/** Korisnik je termine mogao obrisati u kalendaru dok je aplikacija bila u pozadini. */
@Composable
fun ProvjeriNaPovratku(onPovratak: () -> Unit) {
    val vlasnikZivotnogCiklusa = LocalLifecycleOwner.current
    DisposableEffect(vlasnikZivotnogCiklusa) {
        val promatrac = LifecycleEventObserver { _, dogadjaj ->
            if (dogadjaj == Lifecycle.Event.ON_RESUME) onPovratak()
        }
        vlasnikZivotnogCiklusa.lifecycle.addObserver(promatrac)
        onDispose { vlasnikZivotnogCiklusa.lifecycle.removeObserver(promatrac) }
    }
}
