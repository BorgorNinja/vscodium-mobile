package dev.vscodium.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.vscodium.mobile.data.model.EditorSettings
import dev.vscodium.mobile.data.repository.SettingsRepository
import dev.vscodium.mobile.ui.MainScreen
import dev.vscodium.mobile.ui.theme.VSCodiumMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsState(initial = EditorSettings())
            VSCodiumMobileTheme(paletteKey = settings.paletteKey) {
                MainScreen()
            }
        }
    }
}
