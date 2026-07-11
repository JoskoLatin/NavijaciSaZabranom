package com.navijacisazabranom.app.ui.screens.rang

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.navijacisazabranom.app.R
import com.navijacisazabranom.app.ui.theme.NavijaciTheme

private data class RangOption(val id: String, val labelRes: Int)

private val rangOptions = listOf(
    RangOption("supersport-hnl", R.string.rang_supersport_hnl),
    RangOption("prva-nl", R.string.rang_prva_nl),
    RangOption("druga-nl", R.string.rang_druga_nl),
    RangOption("zupanijska-liga", R.string.rang_zupanijska_liga),
)

@Composable
fun RangScreen(onRangSelected: (String) -> Unit) {
    var selectedId by remember { mutableStateOf(rangOptions.first().id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = stringResource(R.string.rang_title), style = MaterialTheme.typography.headlineSmall)

        Column(modifier = Modifier.padding(top = 16.dp)) {
            rangOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = option.id == selectedId,
                            onClick = { selectedId = option.id },
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = option.id == selectedId, onClick = { selectedId = option.id })
                    Text(text = stringResource(option.labelRes), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Button(
            onClick = { onRangSelected(selectedId) },
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(text = stringResource(R.string.action_next))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RangScreenPreview() {
    NavijaciTheme {
        RangScreen(onRangSelected = {})
    }
}
