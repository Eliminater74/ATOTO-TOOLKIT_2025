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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class PkgItem(val pkg: String, val label: String)

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
    var hasShizuku by remember { mutableStateOf(false) }

    // Load once
    LaunchedEffect(Unit) {
        hasRoot = RootShell.isRootAvailable()
        hasShizuku = RootShell.isShizukuAvailable()
        all = loadPackages(ctx)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            "App Debloater", 
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Search and Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                label = { Text("Search apps...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            
            // Refresh
            IconButton(onClick = { scope.launch { all = loadPackages(ctx) } }) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }

        // Selection Tools
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = selected.isNotEmpty(),
                onClick = { selected = emptySet() },
                label = { Text("Clear") },
                leadingIcon = { Icon(Icons.Default.Clear, null) }
            )
            FilterChip(
                selected = false,
                onClick = { 
                     selected = filtered(all, query)
                        .map { it.pkg }
                        .filterNot { it in PROTECTED }
                        .toSet()
                },
                label = { Text("Select All (Safe)") }
            )
            
            // Profiles
            SuggestionChip(
                onClick = { 
                    selected = filtered(all, query)
                        .map { it.pkg }
                        .filter { it in SUGGESTED_SAFE && it !in PROTECTED }
                        .toSet() 
                },
                label = { Text("Safe Bloat") }
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // List
        val shown = filtered(all, query)
        
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(8.dp))
        ) {
            if (shown.isEmpty()) {
                Text(
                    "No apps found.", 
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shown, key = { it.pkg }) { item ->
                        val isSelected = selected.contains(item.pkg)
                        AppItemRow(
                            item = item,
                            isSelected = isSelected,
                            isProtected = item.pkg in PROTECTED,
                            onToggle = { 
                                selected = if (isSelected) selected - item.pkg else selected + item.pkg
                            }
                        )
                    }
                }
            }
            
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val canAct = !busy && selected.isNotEmpty() && (hasRoot || hasShizuku)
            
            dev.eliminater.atoto_toolkit.ui.components.GradientButton(
                text = "Disable",
                enabled = canAct,
                onClick = {
                    scope.launch {
                        busy = true
                        info = runPkgs(ctx, selected, "pm disable-user --user 0 %s", "disable")
                        busy = false
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Uninstall is riskier, maybe valid for root only or requires special care
            if (hasRoot) {
                Button(
                    enabled = canAct,
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(ctx, selected, "pm uninstall --user 0 %s", "uninstall")
                            busy = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                   Text("Delete")
                }
            }

            OutlinedButton(
                enabled = !busy && selected.isNotEmpty(),
                onClick = {
                    scope.launch {
                        busy = true
                        info = runPkgs(ctx, selected, "cmd package install-existing %s && pm enable %s", "restore")
                        busy = false
                    }
                }
            ) {
                Text("Restore")
            }
        }
        
        if (info.isNotBlank()) {
            Text(
                text = info,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (!hasRoot && !hasShizuku) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Root or Shizuku required for modifications.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
internal fun AppItemRow(
    item: PkgItem,
    isSelected: Boolean,
    isProtected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    item.label, 
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.pkg, 
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            if (isProtected) {
                Icon(
                    Icons.Default.Lock, 
                    "Protected", 
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
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
