package com.navijacisazabranom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.navijacisazabranom.app.navigation.AppNavHost
import com.navijacisazabranom.app.ui.theme.NavijaciTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavijaciTheme {
                // Edge-to-edge je od Androida 15 prisilan, a od 16 se ne može ni isključiti:
                // sadržaj crta ispod statusne i navigacijske trake. Razmak se zato dodaje
                // na jednom mjestu, za sve ekrane. Ekrani s vlastitim Scaffoldom moraju
                // isključiti svoje insete (contentWindowInsets = WindowInsets(0)) da se
                // razmak ne zbroji dvaput.
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
