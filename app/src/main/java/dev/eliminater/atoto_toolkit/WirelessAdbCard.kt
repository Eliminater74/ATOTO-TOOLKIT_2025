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

    ElevatedCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ADB over Wi-Fi", style = MaterialTheme.typography.titleMedium)

            Text("Open developer options and (on Android 11+) Wireless debugging. Pair once, then connect from your PC. Works great when the head unit is hard to cable up.")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Developer options") }

                Button(onClick = {
                    val i = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS")
                    } else {
                        // Best-effort on Android 10: opens Dev Options; user enables "ADB over network" if present in ROM.
                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    }
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(i)
                }) { Text("Wireless debugging") }
            }

            Divider()
            Text(
                """
                Quick steps:
                1) Turn on Developer options.
                2) (Android 11+) Open Wireless debugging → Pair device → scan code or enter pairing code.
                3) On PC: `adb pair HOST:PORT CODE` then `adb connect HOST:5555`.
                4) In this app, you can keep using features without USB.
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
