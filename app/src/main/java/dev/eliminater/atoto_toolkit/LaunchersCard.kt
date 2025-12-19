package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Requires LauncherUtils.kt providing:
 *  data class HomeEntry(val packageName: String, val label: String)
 *  fun homeCandidates(ctx: android.content.Context): List<HomeEntry>
 *  fun currentHomeLabel(ctx: android.content.Context): String?
 *  fun openHomeSettings(ctx: android.content.Context)
 *  fun promptHomeChooser(ctx: android.content.Context)
 *  suspend fun setHomeRoot(pkg: String): Pair<Int, String>
 *  suspend fun makeHomeStickyRoot(pkg: String, alsoDisableOthers: Boolean): String
 * And RootShell.isRootAvailable()
 */

@Composable
fun LaunchersCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var hasRoot by remember { mutableStateOf(false) }
    var current by rememberSaveable { mutableStateOf<String?>(null) }
    var candidates by remember { mutableStateOf(listOf<HomeEntry>()) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var log by rememberSaveable { mutableStateOf("") }

    // Initial load
    LaunchedEffect(Unit) {
        hasRoot = RootShell.isRootAvailable()

        runCatching { LauncherUtils.homeCandidates(ctx) }
            .onSuccess { list ->
                candidates = list
                // Prefer Agama / Nova / FCC if present, else first
                selected = list.firstOrNull {
                    val n = it.packageName.lowercase()
                    n.contains("agama") || n.contains("nova") || n.contains("fcc")
                }?.packageName ?: list.firstOrNull()?.packageName
            }
            .onFailure { t -> log = "Error loading launchers: ${t.message}" }

        current = runCatching { LauncherUtils.currentHomeLabel(ctx) }.getOrNull()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Launcher Manager", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Installed Launchers", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Current HOME: ${current ?: "— (chooser not fixed)"}",
                    style = MaterialTheme.typography.bodySmall
                )
    // ... (rest of management logic)
                // Top actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { LauncherUtils.openHomeSettings(ctx) }) {
                        Text("Open Home settings")
                    }
                    OutlinedButton(onClick = {
                        // Try the chooser; fall back to Settings if something throws
                        runCatching { LauncherUtils.promptHomeChooser(ctx) }
                            .onFailure { t ->
                                log = "Chooser fallback: ${t.message}"
                                LauncherUtils.openHomeSettings(ctx)
                            }
                    }) {
                        Text("Show chooser")
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(onClick = {
                        scope.launch {
                            busy = true
                            try {
                                candidates = LauncherUtils.homeCandidates(ctx)
                                current = LauncherUtils.currentHomeLabel(ctx)
                                if (selected !in candidates.map { it.packageName }) {
                                    selected = candidates.firstOrNull()?.packageName
                                }
                            } catch (t: Throwable) {
                                log = "Refresh failed: ${t.message}"
                            } finally {
                                busy = false
                            }
                        }
                    }) {
                        Text("Refresh list")
                    }
                }

                if (busy) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                // Candidates list (Fixed height to avoid nesting scrollables)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    candidates.forEach { entry ->
                        val isSelected = selected == entry.packageName
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selected = entry.packageName }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(selected = isSelected, onClick = { selected = entry.packageName })
                            Column(Modifier.weight(1f)) {
                                Text(entry.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(entry.packageName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (candidates.isEmpty() && !busy) {
                         Text("No launchers found (unusual).", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Root-only actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = hasRoot && !busy && selected != null,
                        onClick = {
                            scope.launch {
                                busy = true
                                try {
                                    val target = selected!!
                                    val (code, out) = LauncherUtils.setHomeRoot(target)
                                    current = LauncherUtils.currentHomeLabel(ctx)
                                    log = "setHomeRoot($target) exit=$code\n$out"
                                } catch (t: Throwable) {
                                    log = "setHomeRoot failed: ${t.message}"
                                } finally {
                                    busy = false
                                }
                            }
                        }
                    ) { Text("Set as HOME (root)") }

                    Button(
                        enabled = hasRoot && !busy && selected != null,
                        onClick = {
                            scope.launch {
                                busy = true
                                try {
                                    val target = selected!!
                                    log = LauncherUtils.makeHomeStickyRoot(
                                        pkg = target,
                                        alsoDisableOthers = true
                                    )
                                    current = LauncherUtils.currentHomeLabel(ctx)
                                } catch (t: Throwable) {
                                    log = "Make sticky failed: ${t.message}"
                                } finally {
                                    busy = false
                                }
                            }
                        }
                    ) { Text("Make sticky (root)") }
                }

                if (!hasRoot) {
                    Text(
                        "Tip: Without root, use the chooser or Home settings to pick your launcher.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (log.isNotBlank()) {
                    HorizontalDivider()
                    Text("Log", style = MaterialTheme.typography.labelMedium)
                    Text(log, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Guide
        LauncherGuide()
    }
}
