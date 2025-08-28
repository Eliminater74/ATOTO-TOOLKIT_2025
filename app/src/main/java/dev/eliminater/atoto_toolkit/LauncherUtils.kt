package dev.eliminater.atoto_toolkit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HomeEntry(
    val label: String,
    val packageName: String,
    val activity: String? = null
)

object LauncherUtils {

    /** List all apps that can handle HOME (stock + 3rd-party). */
    suspend fun homeCandidates(ctx: Context): List<HomeEntry> = withContext(Dispatchers.IO) {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)

        val matches = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        matches.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            val pkg = ai.packageName ?: return@mapNotNull null
            val label = runCatching { ri.loadLabel(pm).toString() }.getOrElse { pkg }
            val comp = ComponentName(ai.packageName, ai.name)
            HomeEntry(label = label, packageName = pkg, activity = comp.flattenToShortString())
        }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    /** Best-effort current HOME app label (non-root). Returns null when the chooser would appear. */
    suspend fun currentHomeLabel(ctx: Context): String? = withContext(Dispatchers.IO) {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)

        val ri = if (Build.VERSION.SDK_INT >= 33) {
            pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(intent, 0)
        } ?: return@withContext null

        // If the resolver/chooser would show, there isn't a fixed default yet.
        if (ri.activityInfo?.packageName == "android") return@withContext null

        runCatching { ri.loadLabel(pm).toString() }.getOrNull()
    }

    /**
     * Non-root: open a reliable system screen where the user can choose the default Home app.
     * This is crash-proof and works on Android 10+ (with vendor fallbacks).
     */
    fun openHomePicker(ctx: Context) {
        val intents = listOf(
            Intent(android.provider.Settings.ACTION_HOME_SETTINGS),
            Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(android.provider.Settings.ACTION_SETTINGS)
        )
        for (i in intents) {
            try {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(i)
                return
            } catch (_: Throwable) {
                // try next fallback
            }
        }
    }

    /**
     * Kept for call-site compatibility. The old implementation could crash on some ROMs.
     * We now just forward to the safe picker flow above.
     */
    fun promptHomeChooser(ctx: Context) = openHomePicker(ctx)

    /** Explicitly open the Home settings page (first fallback of openHomePicker). */
    fun openHomeSettings(ctx: Context) {
        runCatching {
            val i = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        }.onFailure { openHomePicker(ctx) } // broader fallback
    }

    /** Root: set HOME role holder directly (Android 10+). */
    suspend fun setHomeRoot(pkg: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        RootShell.runSmart("cmd role add-role-holder android.app.role.HOME $pkg 0")
    }

    /**
     * Root: remove other HOME holders and (optionally) disable/uninstall them for user 0.
     * Then kick HOME so the change applies immediately.
     */
    suspend fun makeHomeStickyRoot(pkg: String, alsoDisableOthers: Boolean): String =
        withContext(Dispatchers.IO) {
            val sb = StringBuilder()

            val holdersOut = RootShell.runSmart(
                "cmd role get-role-holders android.app.role.HOME"
            ).second.trim()

            val holders = holdersOut
                .split('\n', '\r', ' ', '\t', ',')
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

            // Ensure our target is a holder
            RootShell.runSmart("cmd role add-role-holder android.app.role.HOME $pkg 0")
                .also { sb.appendLine("add-role-holder: exit=${it.first} ${it.second}") }

            // Remove others (and optionally neutralize them)
            holders.filter { it != pkg }.forEach { other ->
                RootShell.runSmart("cmd role remove-role-holder android.app.role.HOME $other 0")
                    .also { sb.appendLine("remove $other: exit=${it.first} ${it.second}") }

                if (alsoDisableOthers) {
                    val u = RootShell.runSmart("pm uninstall --user 0 $other")
                    if (!u.second.contains("Success", ignoreCase = true)) {
                        val d = RootShell.runSmart("pm disable-user --user 0 $other")
                        sb.appendLine("disable $other: exit=${d.first} ${d.second}")
                    } else {
                        sb.appendLine("uninstall-user0 $other: exit=${u.first} ${u.second}")
                    }
                }
            }

            // Kick HOME so chooser/default updates immediately
            RootShell.runSmart("am start -a android.intent.action.MAIN -c android.intent.category.HOME")

            sb.toString()
        }

    /**
     * Root: clear all HOME role holders so the genuine system chooser appears on next HOME.
     * We also fire a HOME intent to nudge the chooser immediately.
     */
    suspend fun forceHomeChooserRoot(ctx: Context): String = withContext(Dispatchers.IO) {
        if (!RootShell.isRootAvailable()) return@withContext "no root"
        val res = RootShell.runSmart("cmd role clear-role-holders android.app.role.HOME")
        runCatching {
            val i = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        }
        "clear-role-holders exit=${res.first}"
    }
}
