package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class RadioAppInfo(
    val rank: Int,
    val name: String,
    val packageName: String,
    val packageAliases: List<String> = emptyList(), // For Free/Pro variants
    val summary: String,
    val fmHardware: String,
    val internet: String,
    val verdict: String,
    val pros: List<String>,
    val cons: List<String>,
    val bestFor: String,
    val playStoreUrl: String,
    val isRecommended: Boolean = false
)

val RECOMMENDED_RADIOS = listOf(
    RadioAppInfo(
        rank = 1,
        name = "NavRadio+",
        packageName = "com.navimods.radio",
        packageAliases = listOf("com.navimods.radio.free", "com.navimods.radio_free"), // Check these too
        summary = "The ONLY true FM hardware app.",
        fmHardware = "✅ Yes (Hardware)",
        internet = "❌ No",
        verdict = "Best hardware radio app",
        pros = listOf("Directly controls FM hardware", "Station logos & detailed metadata", "Highly customizable UI"),
        cons = listOf("Paid App (Basic version available)", "Requires compatible head unit MCU"),
        bestFor = "Replacing the stock FM app with a premium experience.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.navimods.radio",
        isRecommended = true
    ),
    RadioAppInfo(
        rank = 2,
        name = "NextRadio",
        packageName = "com.nextradioapp.nextradio",
        summary = "FM + Online Hybrid.",
        fmHardware = "⚠️ Limited",
        internet = "✅ Yes",
        verdict = "Best combined UI",
        pros = listOf("Great UI", "Massive station database", "RDS support where available"),
        cons = listOf("Often can't access FM hardware on head units", "Best features limited without tuner API"),
        bestFor = "Internet radio replacement with car-style UI.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.nextradioapp.nextradio"
    ),
    RadioAppInfo(
        rank = 3,
        name = "TuneIn Radio",
        packageName = "tunein.player",
        summary = "Oldest & biggest directory.",
        fmHardware = "❌ No",
        internet = "✅ Yes",
        verdict = "Best online station library",
        pros = listOf("Huge station list", "Reliable streaming", "Good car UI"),
        cons = listOf("No FM hardware control", "Some features behind premium"),
        bestFor = "Streaming live stations when online.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=tunein.player"
    ),
    RadioAppInfo(
        rank = 4,
        name = "Simple Radio",
        packageName = "com.streema.simpleradio",
        summary = "Straightforward streaming.",
        fmHardware = "❌ No",
        internet = "✅ Yes",
        verdict = "Fast and light",
        pros = listOf("Lightweight", "Quick find presets"),
        cons = listOf("Internet only", "Minimal extras"),
        bestFor = "Simple, fast online station listening.",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.streema.simpleradio"
    )
)

@Composable
fun RadioGuide() {
    val ctx = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Radio Recommendations",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Hardware FM vs Internet Radio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Most 3rd party apps cannot access the ATOTO FM chip directly due to Android API limitations. They primarily work as Internet Radio apps unless specified.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        RECOMMENDED_RADIOS.forEach { app ->
            RadioAppItem(app) {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(app.playStoreUrl))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { ctx.startActivity(i) }
            }
        }
    }
}

@Composable
private fun RadioAppItem(info: RadioAppInfo, onStore: () -> Unit) {
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
                    Text(info.summary, style = MaterialTheme.typography.bodySmall)
                }
                
                if (info.isRecommended) {
                    Icon(Icons.Default.Star, "Recommended", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Tech Specs Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("FM Tuner", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(info.fmHardware, style = MaterialTheme.typography.bodySmall)
                }
                Column {
                    Text("Internet", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(info.internet, style = MaterialTheme.typography.bodySmall)
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
