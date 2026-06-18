package com.example.myproxy.core

import android.content.Context
import android.os.Build
import java.io.File

class BinaryManager(private val context: Context) {
    fun ensureExecutable(binaryName: String): Result<File> {
        val binDir = File(context.filesDir, "bin").apply { mkdirs() }
        val target = File(binDir, binaryName)
        if (target.exists()) {
            target.setExecutable(true)
            return Result.success(target)
        }

        val copied = copyFromAssets(binaryName, target)
        if (!copied) {
            return Result.failure(
                IllegalStateException(
                    "Missing binary: $binaryName. Put it in app/src/main/assets/bin/<abi>/$binaryName"
                )
            )
        }
        target.setExecutable(true)
        return Result.success(target)
    }

    private fun copyFromAssets(binaryName: String, target: File): Boolean {
        val abis = Build.SUPPORTED_ABIS.toList()
        for (abi in abis) {
            val assetPath = "bin/$abi/$binaryName"
            try {
                context.assets.open(assetPath).use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                return true
            } catch (_: Exception) {
                // Try next ABI candidate.
            }
        }
        return false
    }
}
