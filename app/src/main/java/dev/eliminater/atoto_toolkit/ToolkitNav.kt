package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// 👇 ADD THIS IMPORT
import dev.eliminater.atoto_toolkit.settings.SettingsScreen

/** App destinations (add more as we grow). */
sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Status    : Dest("status",    "Status",    Icons.Outlined.Info)
    data object Quick     : Dest("quick",     "Quick",     Icons.Outlined.Bolt)
    data object Launchers : Dest("launchers", "Launchers", Icons.Outlined.Home)
    data object Shizuku   : Dest("shizuku",   "Shizuku",   Icons.Outlined.Power)
    data object Debloat   : Dest("debloat",   "Debloat",   Icons.Outlined.Delete)
    data object Backup    : Dest("backup",    "Backup",    Icons.Outlined.Save)
    data object Wireless  : Dest("wireless",  "ADB Wi-Fi", Icons.Outlined.Wifi)
    data object Radio     : Dest("radio",     "Radio",     Icons.Filled.Radio) // ADDED THIS
    data object Recs      : Dest("recs",      "Apps",      Icons.Filled.ThumbUp)
    data object Help      : Dest("help",      "Help",      Icons.Outlined.HelpOutline)
    data object About     : Dest("about",     "About",     Icons.Outlined.Info)
    data object Settings  : Dest("settings",  "Settings",  Icons.Outlined.Settings)
    data object RootInfo  : Dest("root_guide", "Root Guide", Icons.Outlined.Info) // Not in sidebar
}

private val destinations = listOf(
    Dest.Status, Dest.Quick, Dest.Launchers, Dest.Radio, Dest.Recs, Dest.Debloat, Dest.Backup, Dest.Shizuku, Dest.Wireless, Dest.Help, Dest.About, Dest.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolkitApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentDest: NavDestination? = backStack?.destination
    
    // Core layout: Row with Sidebar on left, Content on right
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Sidebar
        Column(
            modifier = Modifier
                .width(100.dp) // Wider touch targets
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Icon / branding could go here
            Icon(
                imageVector = Icons.Outlined.Bolt, // Placeholder logo
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(Modifier.height(16.dp))

            destinations.forEach { d ->
                val selected = currentDest?.hierarchy?.any { it.route == d.route } == true
                SidebarItem(
                    dest = d, 
                    selected = selected, 
                    onClick = { nav.navigateSingleTopTo(d.route) }
                )
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) // Glass-ish
        ) {
            NavHost(
                navController = nav,
                startDestination = Dest.Status.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Dest.Status.route)    { StatusCard() }
                composable(Dest.Quick.route)     { QuickActions() }
                composable(Dest.Launchers.route) { LaunchersCard() }
                composable(Dest.Shizuku.route)   { ShizukuCard() }
                composable(Dest.Debloat.route)   { DebloaterCard() }
                composable(Dest.Backup.route)    { BackupCard() }
                composable(Dest.Wireless.route)  { WirelessAdbCard() }
                composable(Dest.Radio.route)     { RadioCard() } // ADDED THIS
                composable(Dest.Recs.route)      { RecommendationsCard() }
                composable(Dest.Help.route)      { 
                    HelpCard(onOpenRootGuide = { nav.navigateSingleTopTo(Dest.RootInfo.route) }) 
                }
                composable(Dest.About.route)     { AboutCard() }
                composable(Dest.Settings.route)  { SettingsScreen() }
                composable(Dest.RootInfo.route)  { RootGuideScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}

@Composable
fun SidebarItem(
    dest: Dest, 
    selected: Boolean, 
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = dest.icon, 
            contentDescription = dest.label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = dest.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}

private fun currentLabel(d: NavDestination?): String? =
    destinations.firstOrNull { dest -> d?.hierarchy?.any { it.route == dest.route } == true }?.label

private fun NavController.navigateSingleTopTo(route: String) =
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) { saveState = true }
    }
