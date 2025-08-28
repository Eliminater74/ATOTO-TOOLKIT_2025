package dev.eliminater.atoto_toolkit

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
import kotlinx.coroutines.launch

@Composable
fun LaunchersCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasRoot by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf<String?>(null) }
    var candidates by remember { mutableStateOf(emptyList<HomeEntry>()) }
    var selected by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        hasRoot = RootShell.isRootAvailable()
        candidates = LauncherUtils.homeCandidates(ctx)
        current = LauncherUtils.currentHomeLabel(ctx)
        // Auto-select a nice default (e.g., Agama/Nova) if present
        selected = candidates.firstOrNull {
            val n = it.packageName.lowercase()
            n.contains("agama") || n.contains("nova") || n.contains("fcc")
        }?.packageName ?: candidates.firstOrNull()?.packageName
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Launchers", style = MaterialTheme.typography.titleMedium)
            Text(
                "Current HOME: ${current ?: "— (not fixed / chooser)"}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    LauncherUtils.openHomeSettings(ctx)
                }) { Text("Open Home settings") }

                OutlinedButton(onClick = {
                    LauncherUtils.promptHomeChooser(ctx)
                }) { Text("Show chooser") }

                Spacer(Modifier.weight(1f))

                OutlinedButton(onClick = {
                    scope.launch {
                        busy = true
                        candidates = LauncherUtils.homeCandidates(ctx)
                        current = LauncherUtils.currentHomeLabel(ctx)
                        if (selected !in candidates.map { it.packageName }) {
                            selected = candidates.firstOrNull()?.packageName
                        }
                        busy = false
                    }
                }) { Text("Refresh list") }
            }

            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(candidates, key = { it.packageName }) { e ->
                    val sel = selected == e.packageName
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selected = e.packageName }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(selected = sel, onClick = { selected = e.packageName })
                        Column(Modifier.weight(1f)) {
                            Text(e.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(e.packageName, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Non-root path: just open settings/chooser (done above)
                Button(
                    enabled = hasRoot && !busy && selected != null,
                    onClick = {
                        scope.launch {
                            busy = true
                            val (code, out) = LauncherUtils.setHomeRoot(selected!!)
                            current = LauncherUtils.currentHomeLabel(ctx)
                            log = "setHomeRoot exit=$code\n$out"
                            busy = false
                        }
                    }
                ) { Text("Set as HOME (root)") }

                Button(
                    enabled = hasRoot && !busy && selected != null,
                    onClick = {
                        scope.launch {
                            busy = true
                            log = LauncherUtils.makeHomeStickyRoot(
                                pkg = selected!!,
                                alsoDisableOthers = true
                            )
                            current = LauncherUtils.currentHomeLabel(ctx)
                            busy = false
                        }
                    }
                ) { Text("Make sticky (root)") }
            }

            if (log.isNotBlank()) {
                Divider()
                Text("Log", style = MaterialTheme.typography.labelMedium)
                Text(log, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
