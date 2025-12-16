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
                        .filter { all.find { p -> p.pkg == it }?.safety != SafetyLevel.UNSAFE }
                        .toSet()
                },
                label = { Text("Select All (Safe)") }
            )
            
            // Profiles
            SuggestionChip(
                onClick = { 
                    selected = filtered(all, query)
                        .map { it.pkg }
                        .filter { all.find { p -> p.pkg == it }?.safety == SafetyLevel.SAFE }
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
                Divider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                
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
        val known = PackageDB.KNOWN_PACKAGES[p]
        if (known?.safety == SafetyLevel.UNSAFE && actionTag != "restore") {
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
