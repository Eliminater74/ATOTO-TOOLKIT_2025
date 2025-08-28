package dev.eliminater.atoto_toolkit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/** App destinations (add more as we grow). */
sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Quick     : Dest("quick",     "Quick",     Icons.Outlined.Bolt)
    data object Launchers : Dest("launchers", "Launchers", Icons.Outlined.Home)
    data object Shizuku   : Dest("shizuku",   "Shizuku",   Icons.Outlined.Power)
    data object Debloat   : Dest("debloat",   "Debloat",   Icons.Outlined.Delete)
    data object Wireless  : Dest("wireless",  "ADB Wi-Fi", Icons.Outlined.Wifi)
}

private val items = listOf(
    Dest.Quick, Dest.Launchers, Dest.Shizuku, Dest.Debloat, Dest.Wireless
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolkitApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentDest: NavDestination? = backStack?.destination
    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentLabel(currentDest) ?: "ATOTO Toolkit") }
            )
        },
        bottomBar = {
            if (!isWide) {
                NavigationBar {
                    items.forEach { d ->
                        val selected = currentDest?.hierarchy?.any { it.route == d.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { nav.navigateSingleTopTo(d.route) },
                            icon = { Icon(d.icon, contentDescription = d.label) },
                            label = { Text(d.label) }
                        )
                    }
                }
            }
        }
    ) { pad ->
        Row(Modifier.padding(pad)) {
            if (isWide) {
                NavigationRail {
                    items.forEach { d ->
                        val selected = currentDest?.hierarchy?.any { it.route == d.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { nav.navigateSingleTopTo(d.route) },
                            icon = { Icon(d.icon, contentDescription = d.label) },
                            label = { Text(d.label) }
                        )
                    }
                }
            }

            NavHost(
                navController = nav,
                startDestination = Dest.Quick.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Dest.Quick.route)     { QuickActions() }
                composable(Dest.Launchers.route) { LaunchersCard() }
                composable(Dest.Shizuku.route)   { ShizukuCard() }
                composable(Dest.Debloat.route)   { DebloaterCard() }     // <- new tab
                composable(Dest.Wireless.route)  { WirelessAdbCard() }   // <- new tab
            }
        }
    }
}

private fun currentLabel(d: NavDestination?): String? =
    items.firstOrNull { dest -> d?.hierarchy?.any { it.route == dest.route } == true }?.label

private fun NavController.navigateSingleTopTo(route: String) =
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) { saveState = true }
    }
