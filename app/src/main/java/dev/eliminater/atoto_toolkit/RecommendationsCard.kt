package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AppRec(
    val name: String,
    val summary: String,
    val details: String,
    val url: String
)

data class RecCategory(
    val title: String,
    val icon: ImageVector,
    val apps: List<AppRec>
)

val RECOMMENDATIONS = listOf(
    RecCategory(
        "Navigation & Maps",
        Icons.Default.Map,
        listOf(
            AppRec(
                "Waze",
                "Best for live traffic & police alerts.",
                "Crowdsourced data makes it unbeatable for avoiding traffic jams and speed traps. UI is a bit cartoonish but very effective.",
                "https://play.google.com/store/apps/details?id=com.waze"
            ),
            AppRec(
                "Google Maps",
                "Best all-rounder & integration.",
                "Standard for a reason. Excellent offline maps (download beforehand), lane guidance, and voice search integration.",
                "https://play.google.com/store/apps/details?id=com.google.android.apps.maps"
            ),
            AppRec(
                "Sygic GPS Navigation",
                "Best for offline usage.",
                "Premium offline maps, 3D buildings, and HUD mode. Great if you often drive in areas with poor data coverage.",
                "https://play.google.com/store/apps/details?id=com.sygic.aura"
            ),
            AppRec(
                "TomTom AmiGO",
                "Ad-free & privacy focused.",
                "A lighter alternative to Waze with speed cam alerts and traffic, but no ads.",
                "https://play.google.com/store/apps/details?id=com.tomtom.speedcams.android.map"
            )
        )
    ),
    RecCategory(
        "OBD2 & Diagnostics",
        Icons.Default.Build,
        listOf(
            AppRec(
                "Torque Pro",
                "The classic OBD2 scanner.",
                "Extremely customizable dashboards. Requires a Bluetooth OBD2 adapter. Great for monitoring temps and boost.",
                "https://play.google.com/store/apps/details?id=org.prowl.torque"
            ),
            AppRec(
                "Car Scanner ELM OBD2",
                "Modern & user-friendly.",
                "Often easier to set up than Torque, with great sensor graphs and error code reading.",
                "https://play.google.com/store/apps/details?id=com.ovz.carscanner"
            )
        )
    ),
    RecCategory(
        "Music & Audio",
        Icons.Default.MusicNote,
        listOf(
            AppRec(
                "Poweramp",
                "Best local music player.",
                "Unmatched audio engine and equalizer. If you play FLAC/MP3 files from USB/SD, this is the one to get.",
                "https://play.google.com/store/apps/details?id=com.maxmpz.audioplayer"
            ),
            AppRec(
                "Spotify",
                "Best streaming library.",
                "Huge library and great 'Car Mode' with larger buttons.",
                "https://play.google.com/store/apps/details?id=com.spotify.music"
            )
        )
    )
)

@Composable
fun RecommendationsCard() {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Recommended Apps", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Text(
            "Curated list of apps that work exceptionally well on head units.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RECOMMENDATIONS.forEach { cat ->
            CategorySection(cat) { url ->
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { ctx.startActivity(i) }
            }
        }
    }
}

@Composable
private fun CategorySection(cat: RecCategory, onOpen: (String) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(cat.icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(cat.title, style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                cat.apps.forEach { app ->
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(app.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(app.details, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                        IconButton(onClick = { onOpen(app.url) }) {
                            @Suppress("DEPRECATION")
                            Icon(Icons.Outlined.OpenInNew, "View in Store")
                        }
                    }
                    if (app != cat.apps.last()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
