package com.example.myproxy.parser

import com.example.myproxy.data.ProxyNode
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object VlessParser {
    fun parseLine(rawLine: String): ProxyNode? {
        val line = rawLine.trim()
        if (line.isBlank() || !line.startsWith("vless://")) return null

        val uri = runCatching { java.net.URI(line) }.getOrNull() ?: return null
        val userInfo = uri.userInfo ?: return null
        val host = uri.host ?: return null
        val port = if (uri.port > 0) uri.port else return null

        val query = parseQuery(uri.rawQuery.orEmpty())
        val protocol = "vless"
        val security = query["security"].orEmpty()
        val network = query["type"].orEmpty()

        val nodeName = uri.fragment?.let { decodeValue(it) }?.takeIf { it.isNotBlank() } ?: host
        return ProxyNode(
            name = nodeName,
            protocol = protocol,
            server = host,
            port = port,
            uuid = userInfo,
            security = security,
            network = network,
            host = decodeValue(query["host"].orEmpty()),
            sni = decodeValue(query["sni"].orEmpty()),
            path = decodeValue(query["path"].orEmpty())
        )
    }

    private fun parseQuery(rawQuery: String): Map<String, String> {
        if (rawQuery.isBlank()) return emptyMap()
        return rawQuery.split("&")
            .mapNotNull { pair ->
                val idx = pair.indexOf("=")
                if (idx <= 0) return@mapNotNull null
                val key = pair.substring(0, idx)
                val value = pair.substring(idx + 1)
                key to value
            }
            .toMap()
    }

    private fun decodeValue(value: String): String {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
        }.getOrDefault(value)
    }
}
