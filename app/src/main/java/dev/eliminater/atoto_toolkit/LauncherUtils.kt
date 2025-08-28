package dev.eliminater.atoto_toolkit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

data class LauncherCandidate(
    val label: String,
    val packageName: String
)

fun homeCandidates(pm: PackageManager): List<LauncherCandidate> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return list.map {
        val label = it.activityInfo.applicationInfo.loadLabel(pm).toString()
        LauncherCandidate(label, it.activityInfo.packageName)
    }.distinctBy { it.packageName }
}

fun currentHomePackage(ctx: Context): String? {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val ri = ctx.packageManager.resolveActivity(intent, 0)
    return ri?.activityInfo?.packageName
}

fun isInstalled(pm: PackageManager, pkg: String): Boolean =
    try { pm.getPackageInfo(pkg, 0); true } catch (_: Exception) { false }

fun openHomeSettings(ctx: Context) {
    ctx.startActivity(
        Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun openPlayStore(ctx: Context, pkg: String) {
    val m1 = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
    val m2 = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
    try { ctx.startActivity(m1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (_: Exception) { ctx.startActivity(m2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
