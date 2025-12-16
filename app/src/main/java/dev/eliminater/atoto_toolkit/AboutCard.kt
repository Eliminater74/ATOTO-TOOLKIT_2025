package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.eliminater.atoto_toolkit.ui.components.GradientButton

@Composable
fun AboutCard() {
    val ctx = LocalContext.current
    
    // Hardcoded for now, or use BuildConfig.VERSION_NAME if properly imported
    val appVersion = "1.0.0-beta" 

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "About",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            "ATOTO Toolkit",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Version $appVersion",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Developer: Eliminater74",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Built for the community with ❤️",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            "Disclaimer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            "This tool is NOT affiliated with ATOTO or any other manufacturer. Features like Debloater and Root operations can modify your system. Always keep a backup.\n\n" +
            "Use at your own risk. The developer is not responsible for looped boots or toasted units.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        GradientButton(
            text = "Visit GitHub Project",
            onClick = {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Eliminater74/ATOTO-TOOLKIT_2025"))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { ctx.startActivity(i) }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
