package dev.eliminater.atoto_toolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.eliminater.atoto_toolkit.settings.ThemePrefs
import dev.eliminater.atoto_toolkit.ui.UiEvent
import dev.eliminater.atoto_toolkit.ui.UiEventBus
import dev.eliminater.atoto_toolkit.ui.theme.ATOTOToolkitTheme
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = LocalContext.current

            // Read the saved theme (SYSTEM on first run)
            val mode: ThemeMode by ThemePrefs.themeFlow(ctx)
                .collectAsState(initial = ThemeMode.SYSTEM)

            // Global snackbar plumbing
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                UiEventBus.events.collect { ev ->
                    if (ev is UiEvent.Snackbar) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = ev.message,
                                actionLabel = ev.actionLabel
                            )
                        }
                    }
                }
            }

            ATOTOToolkitTheme(mode = mode) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                    // If you have a topBar/bottomBar/rail, add them here too
                ) { innerPadding ->
                    // Call your app content inside the Scaffold content area
                    Box(Modifier.padding(innerPadding)) {
                        ToolkitApp()
                    }
                }
            }
        }
    }
}
