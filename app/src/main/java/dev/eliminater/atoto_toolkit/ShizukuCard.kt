package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ShizukuCard() {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val pkg = "moe.shizuku.privileged.api"

    var installed by remember { mutableStateOf(isInstalled(pm, pkg)) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Shizuku (non-root power)", style = MaterialTheme.typography.titleMedium)
            Text(if (installed) "Status: Installed" else "Status: Not installed")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { openPlayStore(ctx, pkg) }) {
                    Text(if (installed) "Open in Play Store" else "Install from Play Store")
                }
                OutlinedButton(onClick = {
                    val launch = pm.getLaunchIntentForPackage(pkg)
                    if (launch != null) ctx.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    else openPlayStore(ctx, pkg)
                }) { Text("Open Shizuku") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    try {
                        ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: Exception) {
                        ctx.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }) { Text("Developer options") }

                OutlinedButton(onClick = {
                    val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                    ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Allow unknown sources") }
            }
        }
    }
}
