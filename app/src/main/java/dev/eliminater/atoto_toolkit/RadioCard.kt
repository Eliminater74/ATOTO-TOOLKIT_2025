package dev.eliminater.atoto_toolkit

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RadioCard() {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()
    
    var installedRadios by remember { mutableStateOf<List<String>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        installedRadios = checkInstalledRadios(ctx)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Radio Replacement", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Installed Radio Apps", style = MaterialTheme.typography.titleMedium)
                
                if (installedRadios.isEmpty()) {
                    Text("No recommended radio apps found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    installedRadios.forEach { pkgName ->
                        val info = RECOMMENDED_RADIOS.find { it.packageName == pkgName }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(info?.name ?: pkgName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tip: You can set these as a shortcut in your launcher or map them to a steering wheel key.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        RadioGuide()
    }
}

private suspend fun checkInstalledRadios(ctx: Context): List<String> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    
    RECOMMENDED_RADIOS.mapNotNull { info ->
        // Check main package OR any alias
        val allPkgs = listOf(info.packageName) + info.packageAliases
        val isInstalled = allPkgs.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
        
        if (isInstalled) info.packageName else null
    }
}
