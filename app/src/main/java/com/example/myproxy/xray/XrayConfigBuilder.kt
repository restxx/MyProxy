package com.example.myproxy.xray

import com.example.myproxy.data.ProxyNode
import org.json.JSONArray
import org.json.JSONObject

object XrayConfigBuilder {
    fun build(node: ProxyNode): String {
        val root = JSONObject()

        root.put("log", JSONObject().put("loglevel", "warning"))
        root.put("inbounds", JSONArray().put(
            JSONObject()
                .put("port", 10808)
                .put("listen", "127.0.0.1")
                .put("protocol", "socks")
                .put("settings", JSONObject().put("udp", true))
        ))

        val wsSettings = JSONObject()
            .put("path", node.path)
            .put("headers", JSONObject().put("Host", node.host))
        val streamSettings = JSONObject()
            .put("network", "ws")
            .put("security", "tls")
            .put("tlsSettings", JSONObject().put("serverName", node.sni))
            .put("wsSettings", wsSettings)

        val user = JSONObject()
            .put("id", node.uuid)
            .put("encryption", "none")

        val outboundVless = JSONObject()
            .put("protocol", "vless")
            .put("settings", JSONObject().put("vnext", JSONArray().put(
                JSONObject()
                    .put("address", node.server)
                    .put("port", node.port)
                    .put("users", JSONArray().put(user))
            )))
            .put("streamSettings", streamSettings)

        root.put("outbounds", JSONArray()
            .put(outboundVless)
            .put(JSONObject().put("protocol", "freedom"))
        )

        return root.toString(2)
    }
}
