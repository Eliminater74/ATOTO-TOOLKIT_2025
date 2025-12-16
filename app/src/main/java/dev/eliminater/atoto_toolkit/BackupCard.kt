package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.eliminater.atoto_toolkit.ui.components.GradientButton
import kotlinx.coroutines.launch

@Composable
fun BackupCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var status by remember { mutableStateOf("Ready") }
    var busy by remember { mutableStateOf(false) }
    var hasRoot by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasRoot = RootShell.isRootAvailable()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Backups & Snapshots",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("System Snapshot", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Saves a list of all installed packages and system properties (getprop). Useful for debugging or before making changes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                GradientButton(
                    text = "Create Snapshot",
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            status = "Creating snapshot..."
                            status = makeSnapshot(ctx)
                            busy = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (hasRoot) {
            Card(
                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("App Data Backup (Root)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Backup internal app data to external storage. This is experimental.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    // Placeholder logic for now, or simple cp logic
                    GradientButton(
                        text = "Backup All User Apps Data",
                        enabled = !busy,
                        onClick = {
                            // TODO: Implement bulk backup logic
                            status = "Feature coming soon!"
                        },
                         modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
             Text(
                "Root required for deep data backups.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        
        Text(status, style = MaterialTheme.typography.bodyMedium)
    }
}
