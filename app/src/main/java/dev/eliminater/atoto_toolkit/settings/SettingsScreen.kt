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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.eliminater.atoto_toolkit.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.material3.ButtonDefaults
import dev.eliminater.atoto_toolkit.ui.UiEventBus
import dev.eliminater.atoto_toolkit.ui.UiEvent
import dev.eliminater.atoto_toolkit.LocalAdb

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
        SelfRepairCard()
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


                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                forceEnableAdb(ctx)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Force Enable ADB")
                    }

                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                forceRestoreUsbMode(ctx)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restore USB Mode")
                    }
                }

                // Debug / Manual Connection
                Button(
                    onClick = {
                        scope.launch {
                            val result = LocalAdb.connect()
                            if (result) {
                                UiEventBus.emit(UiEvent.Snackbar("Connected to Localhost:5555"))
                            } else {
                                UiEventBus.emit(UiEvent.Snackbar("Failed to connect. Is ADB listening?"))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Local Connection (127.0.0.1:5555)")
                }
            }
        }
    }

    private suspend fun forceRestoreUsbMode(ctx: android.content.Context) {
        val sb = StringBuilder()
        
        sb.append("\n=== USB Restore (Switching back to Host) ===\n")
        
        // 1. Find the path again
        val findCmd = "find /sys/devices -name host_dev"
        val rawOutput = runShell(findCmd)
        
        val foundPaths = rawOutput.split("\n")
            .map { it.trim() }
            .filter { it.startsWith("/sys/") && !it.contains("Permission denied") }
        
        if (foundPaths.isNotEmpty()) {
            sb.append("Found paths:\n${foundPaths.joinToString("\n")}\n")
            foundPaths.forEach { path ->
                // Write "host" to switch back to host mode (reading USB sticks)
                val result = writeSysFs(path, "host")
                sb.append("Writing 'host' to $path: $result\n")
            }
        } else {
             sb.append("Search failed. Trying fallback.\n")
             sb.append("Fallback: " + writeSysFs("/sys/devices/platform/soc/soc:ap-ahb/e2500000.usb2/host_dev", "host") + "\n")
        }

        // 2. Also try disabling the manufacturer override
        // As seen in fytadboff.sh: echo '1' > /sys/bus/platform/drivers/usb20_otg/force_usb_mode
        sb.append("Force OTG '1': " + writeSysFs("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", "1") + "\n")
        
        // 3. Stop ADBD? (Optional, maybe user wants wireless to stay involved separately)
        // Let's leave ADBD running but just switch hardware, so wireless might survive.
        
        UiEventBus.emit(UiEvent.Snackbar("Restore Complete:\n$sb"))
    }

    private suspend fun forceEnableAdb(ctx: android.content.Context) {
        val sb = StringBuilder()

        sb.append("=== PHASE 1: Hardware Enable (SysFs) ===\n")
        // MOVE: Hardware must be ON before we try to bind the USB gadget in Phase 3
        val findCmd = "find /sys/devices -name host_dev"
        val rawOutput = runShell(findCmd)
        
        val foundPaths = rawOutput.split("\n")
            .map { it.trim() }
            .filter { it.startsWith("/sys/") && !it.contains("Permission denied") }
        
        if (foundPaths.isNotEmpty()) {
            sb.append("Writing 'device' to ${foundPaths.size} paths...\n")
            foundPaths.forEach { path ->
                val res = writeSysFs(path, "device")
                sb.append("$path -> $res\n")
            }
        } else {
             sb.append("SysFs search failed. Access might be restricted.\n")
             // Blind shot
             writeSysFs("/sys/devices/platform/soc/soc:ap-ahb/20200000.usb/host_dev", "device")
        }

        sb.append("\n=== PHASE 2: Native Manufacturer Scripts ===\n")
        // Try to find and run the dedicated script first
        val fytScript = listOf("/system/bin/fytadbon.sh", "/vendor/bin/fytadbon.sh", "/sbin/fytadbon.sh")
            .find { java.io.File(it).exists() }
        
        if (fytScript != null) {
            sb.append("Found script: $fytScript\n")
            sb.append("Exec: " + runShell("sh $fytScript") + "\n")
        } else {
            sb.append("Native fytadbon.sh not found.\n")
        }

        sb.append("\n=== PHASE 3: Settings Toggle (Wakeup Call) ===\n")
        // Toggle OFF then ON to trigger system observers
        try {
            android.provider.Settings.Global.putInt(ctx.contentResolver, "adb_enabled", 0)
            kotlinx.coroutines.delay(500)
            val result = android.provider.Settings.Global.putInt(ctx.contentResolver, "adb_enabled", 1)
            sb.append("Global.ADB Toggle: ${if(result) "Success" else "Failed"}\n")
        } catch (e: Exception) {
            sb.append("Global.ADB: Error (${e.message})\n")
        }

        try {
            android.provider.Settings.Secure.putInt(ctx.contentResolver, "adb_enabled", 1)
        } catch (e: Exception) { /* Ignore secure failure */ }


        sb.append("\n=== PHASE 4: USB Stack Reset (The Kickstart) ===\n")
        
        // Debug current state
        val preState = runShell("getprop sys.usb.state")
        sb.append("Pre-State: $preState\n")

        // 1. Kill the stack
        runShell("setprop sys.usb.config none")
        kotlinx.coroutines.delay(1000)
        
        // 2. Restart with EXCLUSIVE ADB
        // 'mtp,adb' seems to fail to trigger the daemon on some units.
        // We force 'adb' only. User can use "Restore USB" to get MTP back.
        val res1 = runShell("setprop sys.usb.config adb")
        sb.append("Set 'adb': $res1\n")
        
        // 3. Backup Props
        runShell("setprop persist.sys.usb.config adb")
        runShell("setprop persist.service.adb.enable 1")
        runShell("setprop service.adb.tcp.port 5555")

        kotlinx.coroutines.delay(1000)
        val postState = runShell("getprop sys.usb.state")
        sb.append("Post-State: $postState\n")
        
        // Method 6: Proprietary Wireless Trigger
        sb.append("Prop sys.wl.enable: " + runShell("setprop sys.wl.enable 1") + "\n")

        UiEventBus.emit(UiEvent.Snackbar("Force Sequence Done. Check PC!\n$sb"))
    }
    
    @Composable
    fun SelfRepairCard() {
        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf("Unknown") }
        var isConnected by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isConnected = LocalAdb.isConnected()
            if (!isConnected) {
                // Try to connect silently if port 5555 is open
                isConnected = LocalAdb.connect()
            }
            status = if (isConnected) "Connected (Loopback Active)" else "Not Connected"
        }

        if (isConnected) {
            ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Self-Repair", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Local ADB is active! You can now grant this app the 'WRITE_SECURE_SETTINGS' permission permanently. This fixes 'Phase 2' errors.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(onClick = {
                        scope.launch {
                            val success = LocalAdb.grantSelfPermission(ctx, android.Manifest.permission.WRITE_SECURE_SETTINGS)
                            if (success) {
                                UiEventBus.emit(UiEvent.Snackbar("Success! Permission Granted.\nPhase 2 toggles will now work."))
                            } else {
                                UiEventBus.emit(UiEvent.Snackbar("Failed to grant permission."))
                            }
                        }
                    }) {
                        Text("Grant Permissions to Self")
                    }
                }
            }
        }
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
