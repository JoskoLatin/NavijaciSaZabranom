package com.navijacisazabranom.app.ui.screens.profil

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.data.hns.PraceniKlub
import com.navijacisazabranom.app.data.hns.Utakmica
import com.navijacisazabranom.app.ui.components.DodajUKalendarKartica
import com.navijacisazabranom.app.ui.components.PorukaKalendara
import com.navijacisazabranom.app.ui.components.ProvjeriNaPovratku
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Koliko se ms čeka na sljedeći dodir prije nego se dodir shvati kao običan klik.
 * Kratko, da otvaranje rasporeda ostane brzo, a dovoljno za tri uzastopna dodira.
 */
private const val TROSTRUKI_PROZOR_MS = 350L

/** Do ovoliko dana unaprijed umjesto pukog datuma pišemo "Sutra", "Za 2 dana"… */
private const val ODBROJAVANJE_DANA = 3L

@Composable
fun KlubTab(
    klub: PraceniKlub,
    sljedeca: Utakmica?,
    hnsNaopako: Boolean,
    noviTermini: Int,
    imaTermina: Boolean,
    porukaKalendar: String?,
    onPromijeniKlub: () -> Unit,
    onOtvoriRaspored: () -> Unit,
    onReprezentacija: () -> Unit,
    onPreokreniHns: () -> Unit,
    onDodajSezonu: () -> Unit,
    onPorukaPrikazana: () -> Unit,
    onProvjeriKalendar: () -> Unit,
) {
    ProvjeriNaPovratku(onProvjeriKalendar)
    PorukaKalendara(porukaKalendar, onPorukaPrikazana)

    Column(modifier = Modifier.fillMaxSize()) {
        // Sadržaj kluba skrola samo ako mu zafali mjesta; HNS kartica ostaje pribijena uz tabove.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            KlubZaglavlje(klub = klub, onPromijeniKlub = onPromijeniKlub)

            SljedecaUtakmicaKartica(
                sljedeca = sljedeca,
                onClick = onOtvoriRaspored,
                modifier = Modifier.padding(top = 10.dp),
            )

            DodajUKalendarKartica(
                opis = stringResource(R.string.kalendar_sezona_opis),
                gumbNatpis = stringResource(R.string.kalendar_sezona_gumb),
                noviTermini = noviTermini,
                imaNadolazecih = imaTermina,
                onDodaj = onDodajSezonu,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        ReprezentacijaKartica(
            hnsNaopako = hnsNaopako,
            onOtvori = onReprezentacija,
            onPreokreniHns = onPreokreniHns,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

/** Grb, naziv kluba i natjecanje u jednom retku — promjena kluba je uz njih, a ne preko cijele širine. */
@Composable
private fun KlubZaglavlje(klub: PraceniKlub, onPromijeniKlub: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
        ) {
            AsyncImage(
                model = klub.grbUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_klub_placeholder),
                error = painterResource(R.drawable.ic_klub_placeholder),
                fallback = painterResource(R.drawable.ic_klub_placeholder),
                modifier = Modifier.size(52.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(text = klub.klubNaziv, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = klub.natjecanjeNaziv,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onPromijeniKlub) {
                Text(text = stringResource(R.string.profil_promijeni))
            }
        }
    }
}

/**
 * Za utakmice u sljedećih par dana datum sam po sebi slabo govori koliko je blizu,
 * pa uz njega ide i odbrojavanje ("Sutra", "Za 2 dana"). Dalje od toga nema smisla.
 */
@Composable
private fun odbrojavanje(datum: LocalDate): String? {
    val dana = ChronoUnit.DAYS.between(LocalDate.now(), datum)
    return when {
        dana == 0L -> stringResource(R.string.odbrojavanje_danas)
        dana == 1L -> stringResource(R.string.odbrojavanje_sutra)
        dana in 2L..ODBROJAVANJE_DANA -> stringResource(R.string.odbrojavanje_za_dana, dana.toInt())
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SljedecaUtakmicaKartica(
    sljedeca: Utakmica?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cijela kartica otvara raspored kluba.
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profil_sljedeca_utakmica),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                sljedeca?.datum?.let { datum ->
                    odbrojavanje(datum)?.let { blizina -> OdbrojavanjeOznaka(blizina) }
                }
            }

            if (sljedeca == null) {
                Text(
                    text = stringResource(R.string.profil_nema_sljedece),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                val vrijemeText = sljedeca.vrijeme?.format(timeFormatter)
                    ?: stringResource(R.string.raspored_satnica_tbd)
                Text(
                    text = "${sljedeca.datum.format(dateFormatter)} $vrijemeText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = "${sljedeca.domacinNaziv} — ${sljedeca.gostNaziv}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 2.dp),
                )
                sljedeca.stadion?.let { stadion ->
                    Text(
                        text = stadion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = stringResource(R.string.profil_cijeli_raspored),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun OdbrojavanjeOznaka(tekst: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = tekst,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** HNS logo i poveznica na raspored reprezentacije, u istom retku. */
@Composable
private fun ReprezentacijaKartica(
    hnsNaopako: Boolean,
    onOtvori: () -> Unit,
    onPreokreniHns: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOtvori),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            HnsLogo(naopako = hnsNaopako, onOtvori = onOtvori, onPreokreni = onPreokreniHns)
            Text(
                text = stringResource(R.string.profil_reprezentacija),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun HnsLogo(naopako: Boolean, onOtvori: () -> Unit, onPreokreni: () -> Unit) {
    val kut by animateFloatAsState(
        targetValue = if (naopako) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "hns_naopako",
    )
    var brojDodira by remember { mutableStateOf(0) }

    // Jedan dodir otvara raspored, tri ga okrenu naopako (easter egg). Zato se nakon
    // dodira kratko čeka — tek ako drugi ne stigne, dodir se računa kao običan klik.
    LaunchedEffect(brojDodira) {
        when {
            brojDodira == 0 -> Unit
            brojDodira >= 3 -> {
                onPreokreni()
                brojDodira = 0
            }
            else -> {
                delay(TROSTRUKI_PROZOR_MS)
                if (brojDodira == 1) onOtvori()
                brojDodira = 0
            }
        }
    }

    AsyncImage(
        model = R.drawable.ic_hns,
        contentDescription = stringResource(R.string.profil_reprezentacija),
        modifier = Modifier
            .size(48.dp)
            .rotate(kut)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { brojDodira++ })
            },
    )
}
