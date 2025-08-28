package dev.eliminater.atoto_toolkit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var rootStatus by remember { mutableStateOf("checking…") }
    var output by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        rootStatus = if (RootShell.isRootAvailable()) "Root: OK" else "Root: MISSING"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ATOTO Toolkit (MVP)") },
                actions = { Text(rootStatus, modifier = Modifier.padding(end = 12.dp)) }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Quick actions", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        val (code, txt) = RootShell.runSmart("id && getprop ro.build.display.id")
                        output = "exit=$code\n$txt"
                        busy = false
                    }
                }) { Text("Test shell (root if available)") }

                Button(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        output = try { makeSnapshot(ctx) }
                        catch (e: Throwable) { "Snapshot failed: ${e.message ?: e::class.java.simpleName}" }
                        busy = false
                    }
                }) { Text("Create snapshot") }
            }

            if (busy) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Working…")
                }
            }

            Divider()
            Text("Output", style = MaterialTheme.typography.titleMedium)
            Text(if (output.isBlank()) "—" else output)
        }
    }
}

/** Crash-proof snapshot using app dirs; no suspend calls inside non-suspend lambdas. */
suspend fun makeSnapshot(ctx: Context): String = withContext(Dispatchers.IO) {
    fun ts() = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())

    // Prefer app’s external files (no permission); fallback to internal.
    val baseDir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
    val dir = File(baseDir, "state/backup-${ts()}").apply { mkdirs() }

    // Suspend-safe helper that wraps RootShell.runSmart
    suspend fun runSafe(label: String, cmd: String, fallback: String = "<no output>"): String {
        return try {
            val out = RootShell.runSmart(cmd).second
            if (out.isBlank()) fallback else out
        } catch (t: Throwable) {
            "$label error: ${t.message}"
        }
    }

    val props = runSafe("getprop", "getprop")

    // Try several pm commands using a simple loop (all suspend-safe).
    val candidates = listOf(
        "pm list packages -f",
        "cmd package list packages",
        "pm list packages"
    )
    var pkgsOut = "<no packages output>"
    for (c in candidates) {
        try {
            val out = RootShell.runSmart(c).second
            if (out.isNotBlank()) { pkgsOut = out; break }
        } catch (_: Throwable) {
            // ignore and try next
        }
    }

    // File writes are non-suspend; runCatching is fine here.
    runCatching { File(dir, "getprop.txt").writeText(props) }
        .onFailure { File(dir, "getprop.err.txt").writeText(it.stackTraceToString()) }

    runCatching { File(dir, "packages_full.txt").writeText(pkgsOut) }
        .onFailure { File(dir, "packages_full.err.txt").writeText(it.stackTraceToString()) }

    "Snapshot saved: ${dir.absolutePath}\nFiles: getprop.txt, packages_full.txt"
}
