package dev.eliminater.atoto_toolkit.settings

import androidx.compose.foundation.layout.*
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

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Read the current mode (default to SYSTEM on first run)
    val mode by ThemePrefs.themeFlow(ctx).collectAsState(initial = ThemeMode.SYSTEM)

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
            ) { scope.launch { ThemePrefs.set(ctx, ThemeMode.SYSTEM) } }

            ThemeOptionRow(
                label = "Light",
                selected = mode == ThemeMode.LIGHT
            ) { scope.launch { ThemePrefs.set(ctx, ThemeMode.LIGHT) } }

            ThemeOptionRow(
                label = "Dark",
                selected = mode == ThemeMode.DARK
            ) { scope.launch { ThemePrefs.set(ctx, ThemeMode.DARK) } }
        }
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
