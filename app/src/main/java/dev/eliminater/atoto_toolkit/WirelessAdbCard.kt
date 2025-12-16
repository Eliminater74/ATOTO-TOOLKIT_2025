package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@Composable
fun WirelessAdbCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var rootAvailable by remember { mutableStateOf(false) }
    var adbPort by remember { mutableStateOf("Checking...") }
    var isAdbEnabled by remember { mutableStateOf(false) }

    // Check status
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            rootAvailable = RootShell.isRootAvailable()
            // Check if port is set
            val prop = RootShell.runSmart("getprop service.adb.tcp.port")
            val port = prop.output.map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: "-1"
            isAdbEnabled = port == "5555"
            adbPort = if (isAdbEnabled) "Active (5555)" else "Inactive (USB)"
        }
    }

    fun toggleAdbRoot(enable: Boolean) {
        scope.launch(Dispatchers.IO) {
            if (enable) {
                RootShell.runSmart("setprop service.adb.tcp.port 5555")
                RootShell.runSmart("stop adbd")
                RootShell.runSmart("start adbd")
            } else {
                RootShell.runSmart("setprop service.adb.tcp.port -1")
                RootShell.runSmart("stop adbd")
                RootShell.runSmart("start adbd")
            }
            // Refresh
            val prop = RootShell.runSmart("getprop service.adb.tcp.port")
            val port = prop.output.map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: "-1"
            isAdbEnabled = port == "5555"
            adbPort = if (isAdbEnabled) "Active (5555)" else "Inactive (USB)"
        }
    }

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

        // Status Card
        Card(
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                 Text(
                    "Connection Manager",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Android 10 / Root Handling
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    Text(
                        "On Android 10 (ATOTO S8 standard), Wireless ADB is not a native setting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    if (rootAvailable) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Root Method", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("Status: $adbPort", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isAdbEnabled,
                                onCheckedChange = { toggleAdbRoot(it) }
                            )
                        }
                        Text(
                            "Uses Root to force enable ADB over Wi-Fi on port 5555.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            "Without Root, you MUST use a USB cable first to enable this mode.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    // Android 11+ Native
                    Text(
                        "Android 11+ detected. You can use the native Wireless Debugging menu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    dev.eliminater.atoto_toolkit.ui.components.GradientButton(
                        text = "Dev Options",
                        onClick = {
                            ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        dev.eliminater.atoto_toolkit.ui.components.GradientButton(
                            text = "Native Settings",
                            onClick = {
                                val i = Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS")
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                ctx.startActivity(i)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        
        // Instructions
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("How to Connect", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                
                if (rootAvailable && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                     Text(
                        """
                        1. Toggle the switch above to 'ON'.
                        2. Make sure your PC and Head Unit are on the same Wi-Fi.
                        3. On your PC, run:
                           adb connect IP_ADDRESS:5555
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    Text(
                        """
                        1. Connect Head Unit to PC via USB (Double-DIN USB cable).
                        2. On PC, run:
                           adb tcpip 5555
                        3. Disconnect USB.
                        4. Run:
                           adb connect IP_ADDRESS:5555
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        """
                        1. Tap 'Native Settings' above.
                        2. Enable 'Wireless debugging'.
                        3. Pair with code if needed.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

