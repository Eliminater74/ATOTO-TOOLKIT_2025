package dev.eliminater.atoto_toolkit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.eliminater.atoto_toolkit.ui.components.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootGuideScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rooting Guide (S8 Gen 2)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Disclaimer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "DISCLAIMER: Use at your own risk. This process can brick your device. The developer takes no responsibility for any damage.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                "Based on the method by user 'Eliminater74' and others on XDA.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GuideSection("Prerequisites") {
                Text("• PC with Windows (recommended)")
                Text("• USB A-to-A Male Cable (REQUIRED)")
                Text("• FAT32 formatted USB Flash Drive (32GB or smaller)")
                Text("• ADB & Fastboot Tools installed")
                Text("• Magisk Manager app (apk)")
            }

            GuideSection("1. Preparation") {
                Text("1. Download the latest firmware update for your S8 Gen 2 from ATOTO's website.")
                Text("2. Extract the zip. Locate 'boot.img'.")
                Text("3. Copy 'boot.img' to a device with Magisk (or use this head unit if you can install Magisk apk).")
                Text("4. Open Magisk -> Install -> Select and Patch a File -> Choose 'boot.img'.")
                Text("5. Copy the patched image (e.g., 'magisk_patched.img') back to your PC and rename it to 'boot.img'.")
            }

            GuideSection("2. Flashing") {
                Text("1. Enable Developer Options on head unit.")
                Text("2. Enable 'USB Debugging' and 'OEM Unlocking'.")
                Text("3. USB Mode: Select 'USB computer connection' -> 'File Transfer'.")
                Text("4. Connect PC to the Head Unit's 'Phone Link' USB port using the A-to-A cable.")
                Text("5. Open CMD on PC where adb/fastboot is.")
                Text("6. Run: fastboot flash boot boot.img")
                Text("7. Note: If waiting for device, run the 'adb-fastboot-installer' script or similar to force fastboot mode if available, or reboot bootloader: adb reboot bootloader")
                Text("8. Once flashed, reboot.")
            }

            GuideSection("3. Finalizing") {
                Text("1. Open Magisk app on head unit.")
                Text("2. It may ask to complete additional setup. Select 'Direct Install' if available.")
                Text("3. Reboot.")
                Text("4. You should now be fully rooted.")
            }

            GradientButton(
                text = "Open XDA Thread",
                onClick = {
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://xdaforums.com/t/rooting-atoto-s8-premium-gen2-head-units.4737904/"))
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { ctx.startActivity(i) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GuideSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                content()
            }
        }
    }
}
