package dev.eliminater.atoto_toolkit.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import dev.eliminater.atoto_toolkit.ui.UiEventBus
import dev.eliminater.atoto_toolkit.ui.UiEvent

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Read current theme (defaults to SYSTEM on first run)
    val mode by ThemePrefs.themeFlow(ctx).collectAsState(initial = ThemeMode.SYSTEM)

    fun setMode(newMode: ThemeMode, msg: String) {
        scope.launch {
            ThemePrefs.set(ctx, newMode)
            UiEventBus.emit(UiEvent.Snackbar(msg))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.titleMedium)
                Text("Theme", style = MaterialTheme.typography.titleLarge)

                ThemeOptionRow(
                    label = "Follow system",
                    selected = mode == ThemeMode.SYSTEM
                ) { setMode(ThemeMode.SYSTEM, "Theme set to System") }

                ThemeOptionRow(
                    label = "Light",
                    selected = mode == ThemeMode.LIGHT
                ) { setMode(ThemeMode.LIGHT, "Theme set to Light") }

                ThemeOptionRow(
                    label = "Dark",
                    selected = mode == ThemeMode.DARK
                ) { setMode(ThemeMode.DARK, "Theme set to Dark") }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        setMode(ThemeMode.SYSTEM, "Theme reset to System")
                    }
                ) { Text("Reset theme to System") }
            }
        }

        SettingsSnifferCard()
        
        // Add extra padding at bottom for scrolling
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingsSnifferCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Research Tools", style = MaterialTheme.typography.titleMedium)
            Text("Settings Sniffer", style = MaterialTheme.typography.titleLarge)
            Text(
                "Dump all system settings to a file in Downloads. Use this to find hidden toggles by comparing 'Before' and 'After' snapshots.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    scope.launch {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                        val filename = "atoto_settings_dump_$timestamp.txt"
                        val file = java.io.File("/sdcard/Download/$filename")

                        try {
                            file.writeText("=== GLOBAL ===\n")
                            // Note: We can't easily iterate all keys via public API without reflection or shell. 
                            // Using shell 'settings list' is most reliable for a raw dump.
                            val global = runShell("settings list global")
                            file.appendText(global + "\n\n")

                            file.writeText("=== SYSTEM ===\n")
                            val system = runShell("settings list system")
                            file.appendText(system + "\n\n")

                            file.writeText("=== SECURE ===\n")
                            val secure = runShell("settings list secure")
                            file.appendText(secure + "\n\n")

                            UiEventBus.emit(UiEvent.Snackbar("Saved to Downloads/$filename"))
                        } catch (e: Exception) {
                            UiEventBus.emit(UiEvent.Snackbar("Error: ${e.message}"))
                            e.printStackTrace()
                        }
                    }
                }
            ) {
                Text("Snapshot Settings (Dump to File)")
            }
        }
    }
}

// Simple blocking shell runner for this purpose
fun runShell(cmd: String): String {
    return try {
        val proc = Runtime.getRuntime().exec(cmd)
        proc.inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        "Error running '$cmd': ${e.message}"
    }
}
