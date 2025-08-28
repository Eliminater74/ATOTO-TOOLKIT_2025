package dev.eliminater.atoto_toolkit

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class PkgItem(val pkg: String, val label: String)

/* ---------- Safety sets ---------- */

/** Hard “do-not-touch” list — vendor services + Android/Google essentials */
private val PROTECTED = setOf(
    // FYT / ATOTO glue
    "com.syu.cs","com.syu.ms","com.syu.ps","com.syu.ss","com.syu.us",
    "com.syu.canbus","com.syu.protocolupdate","com.syu.bt","com.syu.steer",
    "com.syu.settings","com.syu.rearcamera",
    // ATOTO keep-alive & GPS
    "com.atoto.keepaliveservice","org.atoto.gps",
    // Android core / critical Google services (partial, keep conservative)
    "android","com.android.systemui","com.android.settings","com.android.permissioncontroller",
    "com.android.providers.settings","com.android.providers.contacts",
    "com.google.android.gms","com.google.android.gsf"
)

/** Suggested “safe removals”: light vendor bloat/UI */
private val SUGGESTED_SAFE = setOf(
    "com.syu.music","com.syu.video","com.syu.gallery","com.syu.filemanager",
    "com.syu.av","com.syu.onekeynavi","com.android.partnerbrowsercustomizations.example"
)

/** Profile: Conservative = SUGGESTED_SAFE */
private val PROFILE_CONSERVATIVE = SUGGESTED_SAFE

/** Profile: No Radio & Mirroring (only if you truly don’t use them) */
private val PROFILE_NO_RADIO_MIRRORING = PROFILE_CONSERVATIVE + setOf(
    "com.syu.carradio", // using NavRadio+ instead
    "net.easyconn","com.syu.carlink","com.syu.carmark","com.synmoon.carkit",
    // extra launchers (disable only after Nova is set as HOME and reboot-tested)
    "com.unisoc.launcher.customization","com.android.launcher3",
    "com.android.launcher6","com.android.launcher8"
    // add "com.google.android.apps.nexuslauncher" if you also want to hide Pixel Launcher
)

/* ---------- UI ---------- */

@Composable
fun DebloaterCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var all by remember { mutableStateOf(listOf<PkgItem>()) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var busy by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }
    var hasRoot by remember { mutableStateOf(false) }

    // Load once
    LaunchedEffect(Unit) {
        hasRoot = RootShell.isRootAvailable()
        all = loadPackages(ctx)
    }

    ElevatedCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Debloater", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by app / package") },
                singleLine = true
            )

            // Row 1: selection helpers
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { selected = emptySet() }) { Text("Clear selection") }
                OutlinedButton(onClick = {
                    // respect search filter and protected guard
                    selected = filtered(all, query)
                        .map { it.pkg }
                        .filterNot { it in PROTECTED }
                        .toSet()
                }) { Text("Select all (filtered)") }

                Spacer(Modifier.weight(1f))

                OutlinedButton(onClick = {
                    scope.launch { all = loadPackages(ctx) }
                }) { Text("Refresh") }
            }

            // Row 2: suggestions / profiles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val safe = SUGGESTED_SAFE
                    selected = filtered(all, query)
                        .map { it.pkg }
                        .filter { it in safe && it !in PROTECTED }
                        .toSet()
                }) { Text("Select suggested (safe)") }

                OutlinedButton(onClick = {
                    selected = filtered(all, query)
                        .map { it.pkg }
                        .filter { it in PROFILE_CONSERVATIVE && it !in PROTECTED }
                        .toSet()
                }) { Text("Apply profile: Conservative") }

                OutlinedButton(onClick = {
                    selected = filtered(all, query)
                        .map { it.pkg }
                        .filter { it in PROFILE_NO_RADIO_MIRRORING && it !in PROTECTED }
                        .toSet()
                }) { Text("Apply profile: No Radio & Mirroring") }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !busy && selected.isNotEmpty() && hasRoot,
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(
                                ctx, selected,
                                "pm disable-user --user 0 %s",
                                "disable-user"
                            )
                            busy = false
                        }
                    }
                ) { Text("Disable (root)") }

                Button(
                    enabled = !busy && selected.isNotEmpty() && hasRoot,
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(
                                ctx, selected,
                                "pm uninstall --user 0 %s",
                                "uninstall-user0"
                            )
                            busy = false
                        }
                    }
                ) { Text("Uninstall for user 0 (root)") }

                OutlinedButton(
                    enabled = !busy && selected.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(
                                ctx, selected,
                                "cmd package install-existing %s && pm enable %s",
                                "restore"
                            )
                            busy = false
                        }
                    }
                ) { Text("Restore") }
            }

            if (busy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                text = if (info.isBlank()) "—" else info,
                style = MaterialTheme.typography.bodySmall
            )

            Divider()

            // Package list
            val shown = filtered(all, query)
            Text("${shown.size} apps", style = MaterialTheme.typography.labelMedium)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(shown, key = { it.pkg }) { item ->
                    val checked = selected.contains(item.pkg)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (checked) selected - item.pkg else selected + item.pkg
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { on ->
                                selected = if (on) selected + item.pkg else selected - item.pkg
                            }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val protectedMark = if (item.pkg in PROTECTED) "  (protected)" else ""
                            Text(item.pkg + protectedMark, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (!hasRoot) {
                Text(
                    "Tip: Without root you can still use Restore, or pair with Shizuku/Wireless ADB for pm commands.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/* ---------- Helpers ---------- */

private fun filtered(list: List<PkgItem>, q: String): List<PkgItem> {
    if (q.isBlank()) return list
    val s = q.trim().lowercase(Locale.ROOT)
    return list.filter { it.pkg.lowercase().contains(s) || it.label.lowercase().contains(s) }
}

private suspend fun loadPackages(ctx: Context): List<PkgItem> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    val infos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.getInstalledPackages(0)
    }
    infos.map { pi ->
        // applicationInfo may be null on newer SDKs; fall back to package name
        val label = pi.applicationInfo?.let { ai ->
            runCatching { pm.getApplicationLabel(ai).toString() }.getOrNull()
        } ?: pi.packageName
        PkgItem(pi.packageName, label)
    }.sortedBy { it.label.lowercase(Locale.ROOT) }
}

private suspend fun runPkgs(
    ctx: Context,
    pkgs: Set<String>,
    cmdFmt: String,
    actionTag: String
): String = withContext(Dispatchers.IO) {
    val base = ctx.getExternalFilesDir(null) ?: ctx.filesDir
    val stateDir = File(base, "state").apply { mkdirs() }
    val log = File(stateDir, "removed_packages.csv").apply {
        if (!exists()) writeText("datetime,action,package,result\n")
    }
    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    val sb = StringBuilder()

    for (p in pkgs) {
        if (p in PROTECTED && actionTag != "restore") {
            sb.appendLine("$p → SKIP (protected)")
            runCatching { log.appendText("$ts,$actionTag,$p,skip-protected\n") }
            continue
        }
        // allow 1 or 2 placeholders (restore path uses it twice)
        val cmd = if (cmdFmt.count { it == '%' } >= 2) cmdFmt.format(p, p) else cmdFmt.format(p)
        val (code, out) = RootShell.runSmart(cmd)
        val line = "$p → exit=$code${if (out.isBlank()) "" else ", $out"}"
        sb.appendLine(line)
        runCatching { log.appendText("$ts,$actionTag,$p,exit=$code\n") }
    }
    sb.toString()
}
