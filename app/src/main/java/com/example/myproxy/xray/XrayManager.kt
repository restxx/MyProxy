package com.example.myproxy.xray

import android.content.Context
import com.example.myproxy.core.BinaryManager
import java.io.File

class XrayManager(private val context: Context) {
    private var process: Process? = null
    private val binaryManager = BinaryManager(context)

    fun writeConfig(content: String): File {
        val configFile = File(context.filesDir, "xray_config.json")
        configFile.writeText(content)
        return configFile
    }

    fun start(configFile: File): Boolean {
        val coreFile = binaryManager.ensureExecutable("xray").getOrNull() ?: return false

        return try {
            coreFile.setExecutable(true)
            process = ProcessBuilder(coreFile.absolutePath, "-config", configFile.absolutePath)
                .redirectErrorStream(true)
                .start()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun stop() {
        process?.destroy()
        process = null
    }
}
