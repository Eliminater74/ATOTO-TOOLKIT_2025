package dev.eliminater.atoto_toolkit

package dev.eliminater.atoto_toolkit

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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
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
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Quick actions", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        val (code, txt) = RootShell.run("id && getprop ro.build.display.id")
                        output = "exit=$code\n$txt"
                        busy = false
                    }
                }) { Text("Test root shell") }

                Button(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        output = makeSnapshot()
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

suspend fun makeSnapshot(): String = withContext(Dispatchers.IO) {
    fun now() = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    val dir = File("/sdcard/Android/data/dev.eliminater.atoto_toolkit/files/state/backup-${now()}").apply { mkdirs() }
    File(dir, "getprop.txt").writeText(RootShell.run("getprop").second)
    File(dir, "packages_full.txt").writeText(RootShell.run("pm list packages -f").second)
    "Snapshot saved: ${dir.absolutePath}\nFiles: getprop.txt, packages_full.txt"
}
