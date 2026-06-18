package com.example.myproxy.speedtest

import com.example.myproxy.data.ProxyNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.system.measureTimeMillis

class SpeedTestManager {
    suspend fun testNode(node: ProxyNode): Int = withContext(Dispatchers.IO) {
        try {
            val elapsed = measureTimeMillis {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(node.server, node.port), 3000)
                }
            }
            elapsed.toInt()
        } catch (_: Exception) {
            -1
        }
    }
}
