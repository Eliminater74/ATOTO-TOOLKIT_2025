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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class PkgItem(
    val pkg: String, 
    val label: String,
    val description: String,
    val safety: SafetyLevel
)

private val PROTECTED_SET = PackageDB.KNOWN_PACKAGES.filter { it.value.safety == SafetyLevel.UNSAFE }.keys

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
    var hasLocalAdb by remember { mutableStateOf(false) }

    // Load once
    LaunchedEffect(Unit) {
        hasRoot = RootShell.isRootAvailable()
        hasLocalAdb = dev.eliminater.atoto_toolkit.LocalAdb.isConnected()
        all = loadPackages(ctx)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ... (Header and Search omitted) ...
        
        // ... (Selection Tools omitted) ...

        // ... (List omitted) ...
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val hasPrivilege = hasRoot || hasLocalAdb
            val canAct = !busy && selected.isNotEmpty()
            
            dev.eliminater.atoto_toolkit.ui.components.GradientButton(
                text = if (hasPrivilege) "Disable Selected" else "Manual Disable",
                enabled = canAct,
                onClick = {
                    if (hasPrivilege) {
                        scope.launch {
                            busy = true
                            info = runPkgs(ctx, selected, "pm disable-user --user 0 %s", "disable", hasLocalAdb)
                            busy = false
                        }
                    } else {
                         // Manual Mode (existing logic)
                         if (selected.size > 1) {
                            info = "For manual disable, please select one app at a time."
                        } else {
                            val pkg = selected.first()
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", pkg, null)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                ctx.startActivity(intent)
                                info = "Opened Settings for $pkg. Click 'Disable' there."
                            } catch (e: Exception) {
                                info = "Error opening settings: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Uninstall is strictly Root/Shizuku only (ADB uninstall requires manual confirmation loop sometimes, stick to disable)
            if (hasPrivilege) {
                Button(
                    enabled = canAct,
                    onClick = {
                        scope.launch {
                            busy = true
                            // If root using pm uninstall, if Shizuku/ADB default to disable
                            val cmd = if (hasRoot) "pm uninstall --user 0 %s" else "pm disable-user --user 0 %s" 
                            val label = if (hasRoot) "uninstall" else "disable"
                            
                            info = runPkgs(ctx, selected, cmd, label, hasLocalAdb)
                            busy = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (hasRoot) "Uninstall" else "Disable+")
                }
            }

            OutlinedButton(
                enabled = !busy && selected.isNotEmpty() && hasPrivilege, // Only automated restore if privileged
                onClick = {
                    scope.launch {
                        busy = true
                        info = runPkgs(ctx, selected, "cmd package install-existing %s && pm enable %s", "restore", hasLocalAdb)
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
        
        if (!hasPrivilege) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "No Root or ADB connection. 'Manual Disable' will open Settings.",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodySmall
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
    onToggle: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                         else MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
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
                
                // Safety Badge
                val (badgeColor, badgeText) = when (item.safety) {
                    SafetyLevel.SAFE -> MaterialTheme.colorScheme.primary to "SAFE"
                    SafetyLevel.CAUTION -> MaterialTheme.colorScheme.tertiary to "CAUTION"
                    SafetyLevel.UNSAFE -> MaterialTheme.colorScheme.error to "UNSAFE"
                    SafetyLevel.UNKNOWN -> MaterialTheme.colorScheme.outline to "UNKNOWN"
                }
                
                Surface(
                    color = badgeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha=0.3f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor
                    )
                }
            }
            
            if (item.description.isNotBlank()) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info, 
                        "Info", 
                        modifier = Modifier.size(14.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
        val p = pi.packageName
        
        // Get friendly label
        var label = pi.applicationInfo?.let { ai ->
            runCatching { pm.getApplicationLabel(ai).toString() }.getOrNull()
        } ?: p

        // Lookup metadata
        val known = PackageDB.KNOWN_PACKAGES[p]
        val desc = known?.description ?: ""
        val saf = known?.safety ?: SafetyLevel.UNKNOWN
        
        PkgItem(p, label, desc, saf)
    }.sortedBy { it.label.lowercase(Locale.ROOT) }
}

private suspend fun runPkgs(
    ctx: Context,
    pkgs: Set<String>,
    cmdFmt: String,
    actionTag: String,
    useLocalAdb: Boolean = false
): String = withContext(Dispatchers.IO) {
    val base = ctx.getExternalFilesDir(null) ?: ctx.filesDir
    val stateDir = File(base, "state").apply { mkdirs() }
    val log = File(stateDir, "removed_packages.csv").apply {
        if (!exists()) writeText("datetime,action,package,result\n")
    }
    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    val sb = StringBuilder()

    for (p in pkgs) {
        val known = PackageDB.KNOWN_PACKAGES[p]
        if (known?.safety == SafetyLevel.UNSAFE && actionTag != "restore") {
            sb.appendLine("$p → SKIP (protected)")
            runCatching { log.appendText("$ts,$actionTag,$p,skip-protected\n") }
            continue
        }
        // allow 1 or 2 placeholders (restore path uses it twice)
        val cmd = if (cmdFmt.count { it == '%' } >= 2) cmdFmt.format(p, p) else cmdFmt.format(p)
        
        val resultString: String
        val code: Int
        
        if (useLocalAdb) {
            // Use Local ADB
            val out = dev.eliminater.atoto_toolkit.LocalAdb.execute(cmd)
            resultString = out
            code = if (out.contains("Success", true) || out.isEmpty()) 0 else 1
        } else {
            // Use Root/ADB/Shell
            val (c, out) = RootShell.runSmart(cmd)
            code = c
            resultString = out
        }
        
        val line = "$p → $code ${if (resultString.isBlank()) "" else "($resultString)"}"
        sb.appendLine(line)
        runCatching { log.appendText("$ts,$actionTag,$p,exit=$code\n") }
    }
    sb.toString()
}
