package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LaunchersCard() {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val scope = rememberCoroutineScope()

    var holders by remember { mutableStateOf(homeCandidates(pm)) }
    var current by remember { mutableStateOf(currentHomePackage(ctx) ?: "unknown") }
    var output by remember { mutableStateOf("") }
    val novaPkg = "com.teslacoilsw.launcher"
    val novaInstalled = remember { isInstalled(pm, novaPkg) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Launchers", style = MaterialTheme.typography.titleMedium)
            Text("Current HOME: $current")
            Text("Candidates:")
            holders.forEach { Text("• ${it.label}  (${it.packageName})") }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { openHomeSettings(ctx) }) { Text("Open Home settings") }
                OutlinedButton(onClick = { holders = homeCandidates(pm); current = currentHomePackage(ctx) ?: "unknown" }) {
                    Text("Refresh list")
                }
            }

            // Root helpers (safe no-ops on emulator)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = novaInstalled,
                    onClick = {
                        scope.launch {
                            val (_, out) = RootShell.runSmart("""cmd role add-role-holder android.app.role.HOME $novaPkg 0""")
                            output = out.ifBlank { "Requested Nova as HOME (root)" }
                            current = currentHomePackage(ctx) ?: current
                        }
                    }
                ) { Text("Set Nova as HOME (root)") }

                OutlinedButton(
                    enabled = novaInstalled,
                    onClick = {
                        scope.launch {
                            val holdersText = RootShell.runSmart("""cmd role get-role-holders android.app.role.HOME""").second
                            val removed = mutableListOf<String>()
                            holdersText.lines().map { it.trim() }.forEach { h ->
                                if (h.isNotEmpty() && h != novaPkg) {
                                    RootShell.runSmart("""cmd role remove-role-holder android.app.role.HOME $h 0""")
                                    RootShell.runSmart("""pm uninstall --user 0 $h || pm disable-user --user 0 $h""")
                                    removed += h
                                }
                            }
                            RootShell.runSmart("""cmd role add-role-holder android.app.role.HOME $novaPkg 0""")
                            output = "Made Nova sticky; removed: ${removed.joinToString().ifBlank { "none" }}"
                            holders = homeCandidates(pm)
                            current = currentHomePackage(ctx) ?: current
                        }
                    }
                ) { Text("Make Nova sticky (root)") }
            }

            if (output.isNotBlank()) {
                Divider()
                Text(output)
            }
        }
    }
}
