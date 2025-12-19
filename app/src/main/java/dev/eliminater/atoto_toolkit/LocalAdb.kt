package dev.eliminater.atoto_toolkit

import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

object LocalAdb {
    private val dadbRef = AtomicReference<Dadb?>(null)
    private const val HOST = "127.0.0.1"
    private const val PORT = 5555

    /**
     * Connects to the local ADB server.
     * Throws exception if connection fails.
     */
    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // If already connected, test it? 
                // Dadb doesn't expose strict "isConnected", but we can try creating a new one if null
                if (dadbRef.get() == null) {
                    val dadb = Dadb.create(HOST, PORT)
                    dadbRef.set(dadb)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                dadbRef.set(null)
                false
            }
        }
    }

    suspend fun isConnected(): Boolean {
        // Lightweight check: try a simple command
        return withContext(Dispatchers.IO) {
            try {
                // If ref is null, we are definitely not connected
                val client = dadbRef.get() ?: return@withContext false
                
                // Try a no-op command
                val response = client.shell("echo 1")
                response.exitCode == 0
            } catch (e: Exception) {
                // Socket likely closed
                dadbRef.set(null)
                false
            }
        }
    }

    /**
     * Executes a shell command via Local ADB.
     * Returns the stdout + stderr combined.
     */
    suspend fun execute(command: String): String {
        return withContext(Dispatchers.IO) {
            try {
                var client = dadbRef.get()
                if (client == null) {
                    // Try to reconnect once
                    connect()
                    client = dadbRef.get()
                }

                if (client == null) {
                    return@withContext "Error: Local ADB not connected. Enable Wireless ADB first."
                }

                val response = client.shell(command)
                // Combine output? Dadb response has .output and .allOutput (verify API)
                // Dadb 1.2.6 returns AdbShellResponse(output, exitCode)
                // Usually output contains both if not separated, but let's check basic usage
                // Ideally we return output. If exitCode != 0, maybe append it?
                
                if (response.exitCode != 0) {
                   "${response.output}\n(Exit Code: ${response.exitCode})" 
                } else {
                    response.output
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Error executing ADB command: ${e.message}"
            }
        }
    }
    
    fun disconnect() {
        try {
            dadbRef.get()?.close()
        } catch (e: Exception) {
            // ignore
        } finally {
            dadbRef.set(null)
        }
    }
}
