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

        ForceAdbCard()
        Spacer(Modifier.height(16.dp))
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
    fun ForceAdbCard() {
        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()

        ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Developer Tools", style = MaterialTheme.typography.titleMedium)
                Text("Force ADB Enable", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Attempt to force ADB ON via internal APIs and Shell Properties. Use this if the system toggle is broken.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            forceEnableAdb(ctx)
                        }
                    }
                ) {
                    Text("Force Enable ADB")
                }
            }
        }
    }

    private suspend fun forceEnableAdb(ctx: android.content.Context) {
        val sb = StringBuilder()
        
        // Method 1: Settings API (Global)
        try {
            val result = android.provider.Settings.Global.putInt(ctx.contentResolver, "adb_enabled", 1)
            sb.append("Global.ADB: ${if(result) "Success" else "Failed"}\n")
        } catch (e: Exception) {
            sb.append("Global.ADB: Error (${e.message})\n")
        }

        // Method 2: Settings API (Secure)
        try {
            val result = android.provider.Settings.Secure.putInt(ctx.contentResolver, "adb_enabled", 1)
            sb.append("Secure.ADB: ${if(result) "Success" else "Failed"}\n")
        } catch (e: Exception) {
            sb.append("Secure.ADB: Error (${e.message})\n")
        }

        // Method 3: Shell Properties (The heavy hitters)
        sb.append("Prop sys.usb.config: " + runShell("setprop sys.usb.config adb") + "\n")
        sb.append("Prop persist.sys.usb.config: " + runShell("setprop persist.sys.usb.config adb") + "\n")
        sb.append("Prop adb.enable: " + runShell("setprop persist.service.adb.enable 1") + "\n")
        
        // Method 4: Wireless ADB (The PC-Free Grail)
        sb.append("Prop tcp.port: " + runShell("setprop service.adb.tcp.port 5555") + "\n")
        sb.append("Prop tcp.port (persist): " + runShell("setprop persist.adb.tcp.port 5555") + "\n")

        // Method 5: Manufacturer Backdoors (SysFs)
        sb.append("\n=== SysFs Path Finder ===\n")
        
        // Search for the file, since the hardcoded paths failed
        val findCmd = "find /sys/devices -name host_dev 2>/dev/null"
        val foundPaths = runShell(findCmd)
        
        if (foundPaths.isNotEmpty() && !foundPaths.contains("Permission denied")) {
            sb.append("Found candidates:\n$foundPaths\n")
            // Attempt to write to found paths dynamically
            foundPaths.trim().split("\n").forEach { path ->
                if (path.isNotEmpty()) {
                    sb.append("Writing to $path: " + writeSysFs(path, "device") + "\n")
                }
            }
        } else {
             sb.append("Search failed or no paths found.\n")
             // Keep the hardcoded ones just in case 'find' failed but paths exist (blind shot)
             sb.append("Path A: " + writeSysFs("/sys/devices/platform/soc/soc:ap-ahb/e2500000.usb2/host_dev", "device") + "\n")
        }


        // Method 6: Proprietary Wireless Trigger (Found in init.syu.rc)
        sb.append("Prop sys.wl.enable: " + runShell("setprop sys.wl.enable 1") + "\n")

        UiEventBus.emit(UiEvent.Snackbar("Force Attempt Complete:\n$sb"))
    }
    
    // Direct file write using Java IO (Bypasses shell restrictions if file is 0666)
    private fun writeSysFs(path: String, value: String): String {
        return try {
            val file = java.io.File(path)
            if (file.exists() && file.canWrite()) {
                file.writeText(value)
                "Success (Java IO)"
            } else {
                // Fallback to shell if Java fails (e.g., restricted parent dir)
                // MUST usage sh -c to allow redirection (>) to work
                val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", "echo '$value' > $path"))
                val stderr = p.errorStream.bufferedReader().use { it.readText() }
                if (stderr.isEmpty()) "Executed (Shell)" else "Shell Error: $stderr"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
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
