package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun WirelessAdbCard() {
    val ctx = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Wireless ADB",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                 Text(
                    "Connection Manager",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Enable Wireless Debugging to manage your device without cables. Essential for non-root modifications.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    dev.eliminater.atoto_toolkit.ui.components.GradientButton(
                        text = "Open Developer Options",
                        onClick = {
                            ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    dev.eliminater.atoto_toolkit.ui.components.GradientButton(
                        text = "Wireless Settings (A11+)",
                        onClick = {
                            val i = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS")
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            }
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(i)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("How to Connect", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    """
                    1. Tap 'Wireless Settings' above.
                    2. Enable 'Wireless debugging'.
                    3. Tap 'Pair device' to get a Wi-Fi pairing code.
                    4. On your PC, run:
                       adb pair IP_ADDRESS:PORT CODE
                    5. Once paired, run:
                       adb connect IP_ADDRESS:PORT
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
