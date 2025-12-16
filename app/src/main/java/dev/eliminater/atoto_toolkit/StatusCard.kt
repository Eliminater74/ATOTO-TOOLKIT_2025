package dev.eliminater.atoto_toolkit

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StatusCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var model by remember { mutableStateOf("Scanning...") }
    var androidVer by remember { mutableStateOf("") }
    var buildId by remember { mutableStateOf("") }
    var rootStatus by remember { mutableStateOf<Boolean?>(null) }
    var shizukuStatus by remember { mutableStateOf<Boolean?>(null) }
    var platform by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            model = Build.MODEL
            androidVer = Build.VERSION.RELEASE
            buildId = Build.DISPLAY
            platform = Build.BOARD
            
            rootStatus = RootShell.isRootAvailable()
            shizukuStatus = RootShell.isShizukuAvailable()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "System Status",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Hardware / ROM Info
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Device Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                
                InfoRow(Icons.Default.Smartphone, "Model", model)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.Android, "Android Version", "Android $androidVer")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.Memory, "Board/Platform", platform)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.Smartphone, "Build ID", buildId)
            }
        }

        // Capabilities (Root/Shizuku)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusChip(
                label = "Root",
                available = rootStatus,
                modifier = Modifier.weight(1f)
            )
            StatusChip(
                label = "Shizuku",
                available = shizukuStatus,
                modifier = Modifier.weight(1f)
            )
        }
        
        Text(
            "This information helps diagnose compatibility and issues. ATOTO S8/TS10 units typically run Android 10 (Q).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusChip(label: String, available: Boolean?, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (available) {
                true -> MaterialTheme.colorScheme.primaryContainer
                false -> MaterialTheme.colorScheme.errorContainer
                null -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = modifier
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (available) {
                    true -> Icons.Outlined.CheckCircle
                    false -> Icons.Outlined.ErrorOutline
                    null -> Icons.Default.Memory // Placeholder
                },
                contentDescription = null,
                tint = when (available) {
                    true -> MaterialTheme.colorScheme.onPrimaryContainer
                    false -> MaterialTheme.colorScheme.onErrorContainer
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$label: ${if (available == true) "Active" else if (available == false) "Missing" else "Checking..."}",
                style = MaterialTheme.typography.labelMedium,
                color = when (available) {
                    true -> MaterialTheme.colorScheme.onPrimaryContainer
                    false -> MaterialTheme.colorScheme.onErrorContainer
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
