package dev.eliminater.atoto_toolkit

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Works in emulator (no root) and on device (root) thanks to RootShell.runSmart(). */
suspend fun makeSnapshot(ctx: Context): String = withContext(Dispatchers.IO) {
    fun ts() = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())

    // Prefer app’s external files (no permission); fallback to internal.
    val baseDir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
    val dir = File(baseDir, "state/backup-${ts()}").apply { mkdirs() }

    // Safe shell helper
    suspend fun runSafe(label: String, cmd: String, fallback: String = "<no output>"): String {
        return try {
            val out = RootShell.runSmart(cmd).second
            if (out.isBlank()) fallback else out
        } catch (t: Throwable) {
            "$label error: ${t.message}"
        }
    }

    val props = runSafe("getprop", "getprop")

    // Try multiple package listing commands (covers emulator/device differences)
    val cmds = listOf(
        "pm list packages -f",
        "cmd package list packages",
        "pm list packages"
    )
    var pkgsOut = "<no packages output>"
    for (c in cmds) {
        try {
            val out = RootShell.runSmart(c).second
            if (out.isNotBlank()) { pkgsOut = out; break }
        } catch (_: Throwable) { /* try next */ }
    }

    // Write files; if a write fails, dump stack to *.err.txt so we don't crash the UI
    runCatching { File(dir, "getprop.txt").writeText(props) }
        .onFailure { File(dir, "getprop.err.txt").writeText(it.stackTraceToString()) }

    runCatching { File(dir, "packages_full.txt").writeText(pkgsOut) }
        .onFailure { File(dir, "packages_full.err.txt").writeText(it.stackTraceToString()) }

    "Snapshot saved: ${dir.absolutePath}\nFiles: getprop.txt, packages_full.txt"
}
