package dev.eliminater.atoto_toolkit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

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

    /**
     * Smart Execution Strategy:
     * 1. Root (Highest Privilege)
     * 2. Local ADB (Privileged User)
     * 3. Shell (Restricted App Sandbox)
     */
    suspend fun runSmart(cmd: String): Pair<Int, String> {
        return when {
            isRootAvailable() -> runRoot(cmd)
            LocalAdb.isConnected() -> {
                // Return 0 for success? LocalAdb.execute implementation needs to support exit codes properly
                // For now, wrapper returns output. We assume success if output !startsWith "Error"
                val out = LocalAdb.execute(cmd)
                if (out.startsWith("Error executing")) -1 to out else 0 to out
            }
            else -> runShell(cmd)
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
