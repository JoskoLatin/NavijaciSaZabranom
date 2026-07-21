package com.navijacisazabranom.app.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.kalendar.KalendarPomocnik

/** Tekstualni gumb (npr. na kartici sljedeće utakmice). */
@Composable
fun DodajUKalendarGumb(utakmica: Utakmica, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    TextButton(onClick = { dodajUKalendar(context, utakmica) }, modifier = modifier) {
        Text(text = stringResource(R.string.action_dodaj_u_kalendar))
    }
}

/** Ikona uz redak u rasporedu. */
@Composable
fun DodajUKalendarIkona(utakmica: Utakmica, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    IconButton(onClick = { dodajUKalendar(context, utakmica) }, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = stringResource(R.string.action_dodaj_u_kalendar),
        )
    }
}

private fun dodajUKalendar(context: Context, utakmica: Utakmica) {
    val intent = KalendarPomocnik.intentZaUtakmicu(
        utakmica = utakmica,
        naslov = "${utakmica.domacinNaziv} — ${utakmica.gostNaziv}",
        opis = context.getString(R.string.kalendar_opis),
    )
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, R.string.kalendar_nema_aplikacije, Toast.LENGTH_SHORT).show()
        }
}
