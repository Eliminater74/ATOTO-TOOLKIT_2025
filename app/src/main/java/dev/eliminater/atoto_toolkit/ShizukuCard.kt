package dev.eliminater.atoto_toolkit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val PKG_SHIZUKU = "moe.shizuku.privileged.api" // main package
private const val PKG_SHIZUKU_ALT = "moe.shizuku.api"        // alt/legacy package

@Composable
fun ShizukuCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var installed by remember { mutableStateOf(false) }
    var root by remember { mutableStateOf<Boolean?>(null) }
    var log by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        installed = isInstalled(ctx, PKG_SHIZUKU) || isInstalled(ctx, PKG_SHIZUKU_ALT)
        root = RootShell.isRootAvailable()
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Shizuku (no-root power)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Status: " +
                        (if (installed) "Installed" else "Not installed") +
                        " • Root: " + when (root) { true -> "available"; false -> "not available"; else -> "checking…" },
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val pkg = if (isInstalled(ctx, PKG_SHIZUKU)) PKG_SHIZUKU else PKG_SHIZUKU_ALT
                    openShizukuOrPlay(ctx, pkg)
                }) {
                    Text(if (installed) "Open Shizuku" else "Install Shizuku")
                }
                OutlinedButton(onClick = { openUrl(ctx, "https://github.com/RikkaApps/Shizuku/releases") }) {
                    Text("GitHub")
                }
                OutlinedButton(onClick = { openDevOptions(ctx) }) { Text("Developer options") }
                OutlinedButton(onClick = { openWirelessAdb(ctx) }) { Text("Wireless ADB") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { openUrl(ctx, "https://topjohnwu.github.io/Magisk/") }) {
                    Text("How root works")
                }
                OutlinedButton(onClick = {
                    scope.launch {
                        log = if (RootShell.isRootAvailable()) "Root: OK" else "Root: missing"
                    }
                }) { Text("Check root") }
            }

            if (log.isNotBlank()) {
                Text(log, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/* ---------- helpers ---------- */

private fun isInstalled(ctx: Context, pkg: String): Boolean =
    try { ctx.packageManager.getPackageInfo(pkg, 0); true }
    catch (_: PackageManager.NameNotFoundException) { false }

private fun openShizukuOrPlay(ctx: Context, pkg: String) {
    val pm = ctx.packageManager
    val launch = pm.getLaunchIntentForPackage(pkg)
    if (launch != null) {
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(launch)
    } else {
        // Play Store, then web fallback
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { ctx.startActivity(market) }
        catch (_: Exception) {
            ctx.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

private fun openUrl(ctx: Context, url: String) {
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun openDevOptions(ctx: Context) {
    ctx.startActivity(
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private fun openWirelessAdb(ctx: Context) {
    val i = if (Build.VERSION.SDK_INT >= 30)
        Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS")
    else
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(i)
}
