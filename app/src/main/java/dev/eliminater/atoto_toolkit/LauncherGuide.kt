package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class LauncherInfo(
    val rank: Int,
    val name: String,
    val summary: String,
    val verdict: String,
    val pros: List<String>,
    val cons: List<String>,
    val bestFor: String,
    val playStoreUrl: String,
    val isRecommended: Boolean = false
)

private val LAUNCHERS = listOf(
    LauncherInfo(
        rank = 1,
        name = "AGAMA Car Launcher",
        summary = "Best Overall for ATOTO S8.",
        verdict = "Best stability & performance",
        pros = listOf("Excellent performance on S8", "Instant cold boot recovery", "Large buttons", "Minimal RAM usage"),
        cons = listOf("Less 'build-your-own' than CWG", "Fixed-style widgets"),
        bestFor = "Factory-clean, fast, and reliable feel.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=altergames.carlauncher",
        isRecommended = true
    ),
    LauncherInfo(
        rank = 2,
        name = "CarWebGuru",
        summary = "Power User / Custom Cockpit.",
        verdict = "Ultimate customization",
        pros = listOf("Unmatched customization", "Dashboard skins", "GPS speedometer", "Full widget layering"),
        cons = listOf("Heavier RAM usage", "Some skins need DPI tweaks", "Can feel 'busy'"),
        bestFor = "Custom race-car / sci-fi cockpit.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.softartstudio.carwebguru"
    ),
    LauncherInfo(
        rank = 3,
        name = "FCC Car Launcher",
        summary = "Widget Powerhouse.",
        verdict = "Widget powerhouse",
        pros = listOf("Widget grid system", "Highly configurable", "Good for OBD2 widgets"),
        cons = listOf("Widgets can misbehave after sleep", "Needs manual resizing for ATOTO"),
        bestFor = "Modular dashboard with lots of widgets.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=ru.speedfire.flycontrolcenter"
    ),
    LauncherInfo(
        rank = 4,
        name = "Car Launcher Pro",
        summary = "Traditional car-style layout.",
        verdict = "Basic & dated",
        pros = listOf("Traditional layout", "Trip computer features"),
        cons = listOf("UI feels dated", "Slower redraw", "Less flexible"),
        bestFor = "Basic, traditional car launcher.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.autolauncher.motorcar.free"
    ),
    LauncherInfo(
        rank = 5,
        name = "AutoZen",
        summary = "Phone-first dashboard.",
        verdict = "Not head-unit friendly",
        pros = listOf("Clean UI"),
        cons = listOf("Poor scaling on head units", "No true launcher behavior", "Breaks immersion"),
        bestFor = "Phone-on-dash setups only.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.autozen.app"
    )
)

@Composable
fun LauncherGuide() {
    val ctx = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Launcher Recommendations",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            "Ranked specifically for ATOTO S8 hardware based on stability, boot behavior, and usability.",
            style = MaterialTheme.typography.bodyMedium
        )

        LAUNCHERS.forEach { launcher ->
            LauncherItem(launcher) {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(launcher.playStoreUrl))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { ctx.startActivity(i) }
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Avoid Generic Launchers", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                Text("Many generic or free car launchers found on the Play Store have poor optimization for ATOTO hardware, causing sleep/wake crashes or inconsistent DPI handling.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun LauncherItem(info: LauncherInfo, onStore: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (info.isRecommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Badge
                Surface(
                    color = if (info.isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "#${info.rank}",
                            color = if (info.isRecommended) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Text(info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(info.summary, style = MaterialTheme.typography.bodyMedium)
                }
                
                if (info.isRecommended) {
                    Icon(Icons.Default.Star, "Recommended", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Details
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Pros:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    info.pros.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
                Column(Modifier.weight(1f)) {
                    Text("Cons:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    info.cons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Best for: ${info.bestFor}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                
                TextButton(onClick = onStore) {
                    @Suppress("DEPRECATION")
                    Icon(Icons.Outlined.OpenInNew, null, modifier=Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Play Store")
                }
            }
        }
    }
}
