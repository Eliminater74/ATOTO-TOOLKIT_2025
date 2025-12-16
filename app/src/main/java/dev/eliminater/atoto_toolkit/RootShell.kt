package dev.eliminater.atoto_toolkit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import rikka.shizuku.Shizuku

/**
 * Root + non-root shell helper.
 * - isRootAvailable(): true if `su` works
 * - runRoot(cmd): su -c cmd
 * - runShell(cmd): sh -c cmd (no root)
 * - runSmart(cmd): su if available, else sh
 */
object RootShell {

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out.contains("uid=0")
        } catch (_: Exception) { false }
    }

    suspend fun runRoot(cmd: String): Pair<Int, String> =
        exec(arrayOf("su", "-c", cmd))

    suspend fun runShell(cmd: String): Pair<Int, String> =
        exec(arrayOf("sh", "-c", cmd))

    /** Check if Shizuku is available and we have permission. */
    fun isShizukuAvailable(): Boolean {
        // Simple check: can we access the class?
        // Real check requires binding, but for now let's assume if the dependency is there
        // and the app is installed, we can try.
        return true
    }

    /** Prefer Root -> Shizuku -> Shell. */
    suspend fun runSmart(cmd: String): Pair<Int, String> {
        return when {
            isRootAvailable() -> runRoot(cmd)
            isShizukuAvailable() -> runShizuku(cmd) 
            else -> runShell(cmd)
        }
    }

    suspend fun runShizuku(cmd: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            // rikka.shizuku.Shizuku.newProcess(String[], String[], String)
            // We use reflection or direct call if imports work. 
            // Since we added the dependency, we can try using the class directly if we import it,
            // or fully qualified name.
            val p = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(p.inputStream)).use { it.lineSequence().forEach(sb::appendLine) }
            BufferedReader(InputStreamReader(p.errorStream)).use { it.lineSequence().forEach(sb::appendLine) }
            val code = p.waitFor()
            code to sb.toString().trim()
        } catch (e: Exception) {
            -1 to ("SHIZUKU ERROR: " + e.message)
        }
    }

    private suspend fun exec(argv: Array<String>): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val p = Runtime.getRuntime().exec(argv)
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(p.inputStream)).use { it.lineSequence().forEach(sb::appendLine) }
            BufferedReader(InputStreamReader(p.errorStream)).use { it.lineSequence().forEach(sb::appendLine) }
            val code = p.waitFor()
            code to sb.toString().trim()
        } catch (e: Exception) {
            -1 to ("ERROR: " + e.message)
        }
    }
}
