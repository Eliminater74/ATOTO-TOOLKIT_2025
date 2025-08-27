package dev.eliminater.atoto_toolkit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Minimal root shell helper.
 * - isRootAvailable(): true if `su -c id` returns uid=0
 * - run(cmd): executes a command via `su -c`, returns (exitCode, combined output)
 */
object RootShell {

    /** Quick check for root access. */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }

    /** Execute a command with root. Returns Pair<exitCode, stdout+stderr>. */
    suspend fun run(cmd: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))

            val sb = StringBuilder()
            BufferedReader(InputStreamReader(p.inputStream)).use { r ->
                r.lineSequence().forEach { sb.appendLine(it) }
            }
            BufferedReader(InputStreamReader(p.errorStream)).use { r ->
                r.lineSequence().forEach { sb.appendLine(it) }
            }

            val code = p.waitFor()
            code to sb.toString().trim()
        } catch (e: Exception) {
            -1 to ("ERROR: " + e.message)
        }
    }
}
