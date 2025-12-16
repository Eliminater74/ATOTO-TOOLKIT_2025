package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelpCard() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.HelpOutline, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                "Help & Guides",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HelpSection(
            title = "Root Access",
            content = "For full functionality, your head unit should be rooted. This allows the app to uninstall system packages and backup data directly."
        )

        HelpSection(
            title = "Non-Root Users",
            content = "You can use this app with Shizuku or Wireless ADB. First, install Shizuku and start it via Wireless Debugging. This grants the app 'special' powers to disable bloatware safely."
        )

        HelpSection(
            title = "Debloater",
            content = "Use 'Safe Bloat' profile to remove common junk without breaking things. 'Protected' apps are hidden to prevent accidental soft-bricks."
        )
        
        HelpSection(
            title = "Backups",
            content = "Always create a 'Snapshot' before making changes. This saves a list of your installed apps so you can see what changed later."
        )
    }
}

@Composable
fun HelpSection(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
