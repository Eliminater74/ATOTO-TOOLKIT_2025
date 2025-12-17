package dev.eliminater.atoto_toolkit.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import dev.eliminater.atoto_toolkit.ui.UiEventBus
import dev.eliminater.atoto_toolkit.ui.UiEvent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Read current theme (defaults to SYSTEM on first run)
    val mode by ThemePrefs.themeFlow(ctx).collectAsState(initial = ThemeMode.SYSTEM)

    fun setMode(newMode: ThemeMode, msg: String) {
        scope.launch {
            ThemePrefs.set(ctx, newMode)
            UiEventBus.emit(UiEvent.Snackbar(msg))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.titleMedium)
                Text("Theme", style = MaterialTheme.typography.titleLarge)

                ThemeOptionRow(
                    label = "Follow system",
                    selected = mode == ThemeMode.SYSTEM
                ) { setMode(ThemeMode.SYSTEM, "Theme set to System") }

                ThemeOptionRow(
                    label = "Light",
                    selected = mode == ThemeMode.LIGHT
                ) { setMode(ThemeMode.LIGHT, "Theme set to Light") }

                ThemeOptionRow(
                    label = "Dark",
                    selected = mode == ThemeMode.DARK
                ) { setMode(ThemeMode.DARK, "Theme set to Dark") }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        setMode(ThemeMode.SYSTEM, "Theme reset to System")
                    }
                ) { Text("Reset theme to System") }
            }
        }

        SettingsSnifferCard()
        
        // Add extra padding at bottom for scrolling
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}



@Composable
fun SettingsSnifferCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Launcher not needed for app-specific paths
    
    ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Research Tools", style = MaterialTheme.typography.titleMedium)
            Text("Settings Sniffer", style = MaterialTheme.typography.titleLarge)
            Text(
                "Dump system settings to App Storage.\nLocation: /sdcard/Android/data/.../files/Download/",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    performDumpDirect(ctx, scope)
                }
            ) {
                Text("Snapshot Settings (Direct Save)")
            }
        }
    }
}



private fun performDumpDirect(ctx: android.content.Context, scope: kotlinx.coroutines.CoroutineScope) {
    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
        val filename = "atoto_settings_dump_$timestamp.txt"
        var finalFile: java.io.File? = null
        
        // Strategy 1: Try Public Downloads (Standard /sdcard/Download)
        // Best for user visibility
        try {
            val publicDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (publicDir.exists() || publicDir.mkdirs()) {
                val candidate= java.io.File(publicDir, filename)
                writeDataToFile(ctx, candidate)
                finalFile = candidate
            }
        } catch (e: Exception) {
            // Permission denied or other error
            e.printStackTrace()
        }

        // Strategy 2: Fallback to App-Specific Storage if public failed
        if (finalFile == null) {
            try {
                val privateDir = ctx.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (privateDir != null) {
                    val candidate = java.io.File(privateDir, filename)
                    writeDataToFile(ctx, candidate)
                    finalFile = candidate
                }
            } catch (e: Exception) {
                UiEventBus.emit(UiEvent.Snackbar("Critical Error: Could not write to ANY storage."))
                e.printStackTrace()
                return@launch
            }
        }

        if (finalFile != null) {
             UiEventBus.emit(UiEvent.Snackbar("Saved to: ${finalFile!!.absolutePath}"))
        }
    }
}

private fun writeDataToFile(ctx: android.content.Context, file: java.io.File) {
    file.bufferedWriter().use { writer ->
        writer.write("=== METHOD 1: ANDROID API (ContentResolver) ===\n")
        writer.write("\n--- SYSTEM ---\n")
        writer.write(dumpTable(ctx, android.provider.Settings.System.CONTENT_URI))
        
        writer.write("\n--- GLOBAL ---\n")
        writer.write(dumpTable(ctx, android.provider.Settings.Global.CONTENT_URI))
        
        writer.write("\n--- SECURE ---\n")
        writer.write(dumpTable(ctx, android.provider.Settings.Secure.CONTENT_URI))

        writer.write("\n\n===========================================\n")
        writer.write("=== METHOD 2: SHELL COMMAND (settings list) ===\n")
        
        writer.write("\n--- GLOBAL ---\n")
        writer.write(runShell("settings list global"))
        writer.write("\n\n")

        writer.write("--- SYSTEM ---\n")
        writer.write(runShell("settings list system"))
        writer.write("\n\n")

        writer.write("--- SECURE ---\n")
        writer.write(runShell("settings list secure"))
        writer.write("\n\n")

        writer.write("=== METHOD 3: SYSTEM PROPERTIES (getprop) ===\n")
        writer.write("This reveals low-level driver configs like sys.usb.config\n\n")
        writer.write(runShell("getprop"))
        writer.write("\n\n")
    }
}

// Queries the internal Settings database directly.
// This often bypasses shell restrictions on non-rooted devices.
private fun dumpTable(ctx: android.content.Context, uri: android.net.Uri): String {
    val sb = StringBuilder()
    try {
        val cursor = ctx.contentResolver.query(uri, null, null, null, "name ASC")
        cursor?.use {
            val nameCol = it.getColumnIndex("name")
            val valCol = it.getColumnIndex("value")
            while (it.moveToNext()) {
                val name = if (nameCol >= 0) it.getString(nameCol) else "?"
                val value = if (valCol >= 0) it.getString(valCol) else "?"
                sb.append("$name=$value\n")
            }
        }
    } catch (e: Exception) {
        sb.append("Error querying table: ${e.message}")
    }
    return sb.toString()
}

// Robust shell runner that captures SDTOUT and STDERR
fun runShell(cmd: String): String {
    return try {
        val proc = Runtime.getRuntime().exec(cmd)
        val stdout = proc.inputStream.bufferedReader().use { it.readText() }
        val stderr = proc.errorStream.bufferedReader().use { it.readText() }
        
        if (stderr.isNotEmpty()) {
            "STDOUT:\n$stdout\nSTDERR:\n$stderr"
        } else {
            stdout
        }
    } catch (e: Exception) {
        "EXEC_ERROR: ${e.message}"
    }
}
