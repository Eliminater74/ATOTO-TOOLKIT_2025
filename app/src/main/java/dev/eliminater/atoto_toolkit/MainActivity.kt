package dev.eliminater.atoto_toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import dev.eliminater.atoto_toolkit.settings.ThemePrefs
import dev.eliminater.atoto_toolkit.ui.theme.ATOTOToolkitTheme
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = LocalContext.current
            // Read the saved theme (SYSTEM on first run)
            val mode: ThemeMode by ThemePrefs.themeFlow(ctx)
                .collectAsState(initial = ThemeMode.SYSTEM)

            ATOTOToolkitTheme(mode = mode) {
                ToolkitApp()
            }
        }
    }
}
