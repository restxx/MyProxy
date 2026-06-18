package com.example.myproxy.vpn

import android.content.Context
import com.example.myproxy.core.BinaryManager

class Tun2SocksManager(context: Context) {
    private val binaryManager = BinaryManager(context)
    private var process: Process? = null

    fun start(tunFd: Int): Boolean {
        val tun2socks = binaryManager.ensureExecutable("tun2socks").getOrNull() ?: return false
        return try {
            process = ProcessBuilder(
                tun2socks.absolutePath,
                "--netif-ipaddr", "10.0.0.2",
                "--netif-netmask", "255.255.255.0",
                "--socks-server-addr", "127.0.0.1:10808",
                "--tunfd", tunFd.toString(),
                "--tunmtu", "1500",
                "--loglevel", "warning"
            ).redirectErrorStream(true).start()
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
