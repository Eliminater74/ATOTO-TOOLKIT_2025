package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Tiny widget you can drop anywhere in your existing UI. */
@Composable
fun QuickActions() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var rootStatus by remember { mutableStateOf("checking…") }
    var busy by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        rootStatus = if (RootShell.isRootAvailable()) "Root: OK" else "Root: MISSING"
    }

    ElevatedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Quick actions", style = MaterialTheme.typography.titleMedium)

            AssistChip(
                onClick = {},
                label = { Text(rootStatus) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        val (code, txt) = RootShell.runSmart("id && getprop ro.build.display.id")
                        output = "exit=$code\n$txt"
                        busy = false
                    }
                }) { Text("Test shell") }

                Button(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        // Uses the single shared makeSnapshot(ctx) defined elsewhere in the package
                        output = try { makeSnapshot(ctx) } catch (t: Throwable) {
                            "Snapshot failed: ${t.message ?: t::class.java.simpleName}"
                        }
                        busy = false
                    }
                }) { Text("Create snapshot") }
            }

            if (busy) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                    Spacer(Modifier.size(10.dp))
                    Text("Working…")
                }
            }

            Divider()
            Text("Output", style = MaterialTheme.typography.titleMedium)
            Text(if (output.isBlank()) "—" else output)
        }
    }
}
