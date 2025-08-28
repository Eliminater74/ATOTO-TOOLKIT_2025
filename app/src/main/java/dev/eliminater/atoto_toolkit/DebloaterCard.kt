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

@Composable
fun DebloaterCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var all by remember { mutableStateOf(listOf<PkgItem>()) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var busy by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    selected = emptySet()
                }) { Text("Clear selection") }

                OutlinedButton(onClick = {
                    selected = filtered(all, query).map { it.pkg }.toSet()
                }) { Text("Select all (filtered)") }

                Spacer(Modifier.weight(1f))

                OutlinedButton(onClick = {
                    // refresh list
                    scope.launch { all = loadPackages(ctx) }
                }) { Text("Refresh") }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val hasRoot = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { hasRoot.value = RootShell.isRootAvailable() }

                Button(
                    enabled = !busy && selected.isNotEmpty() && hasRoot.value,
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(ctx, selected, "pm disable-user --user 0 %s", "disable-user")
                            busy = false
                        }
                    }
                ) { Text("Disable (root)") }

                Button(
                    enabled = !busy && selected.isNotEmpty() && hasRoot.value,
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(ctx, selected, "pm uninstall --user 0 %s", "uninstall-user0")
                            busy = false
                        }
                    }
                ) { Text("Uninstall for user 0 (root)") }

                OutlinedButton(
                    enabled = !busy && selected.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            busy = true
                            info = runPkgs(ctx, selected,
                                "cmd package install-existing %s && pm enable %s", "restore")
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
            val filtered = filtered(all, query)
            Text(
                "${filtered.size} apps",
                style = MaterialTheme.typography.labelMedium
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.pkg }) { item ->
                    val checked = selected.contains(item.pkg)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (checked)
                                    selected - item.pkg else selected + item.pkg
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = checked, onCheckedChange = {
                            selected = if (it) selected + item.pkg else selected - item.pkg
                        })
                        Column(Modifier.weight(1f)) {
                            Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.pkg, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (!RootShell.isRootAvailable()) {
                Text(
                    "Tip: For non-root, you can use Shizuku or Wireless ADB to run the same pm commands.",
                    style = MaterialTheme.typography.bodySmall
                )
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
        val label = runCatching { pm.getApplicationLabel(pi.applicationInfo).toString() }
            .getOrElse { pi.packageName }
        PkgItem(pi.packageName, label)
    }.sortedBy { it.label.lowercase(Locale.ROOT) }
}

private suspend fun runPkgs(
    ctx: Context,
    pkgs: Set<String>,
    cmdFmt: String,
    actionTag: String
): String = withContext(Dispatchers.IO) {
    val sb = StringBuilder()
    val base = ctx.getExternalFilesDir(null) ?: ctx.filesDir
    val log = File(base, "state/removed_packages.csv").apply {
        parentFile?.mkdirs()
        if (!exists()) writeText("datetime,action,package\n")
    }
    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    for (p in pkgs) {
        val cmd = cmdFmt.format(p, p) // supports both %s %s (restore) and single %s
        val (code, out) = RootShell.runSmart(cmd)
        sb.appendLine("$p → exit=$code, $out")
        // log
        runCatching { log.appendText("$ts,$actionTag,$p\n") }
    }
    sb.toString()
}
