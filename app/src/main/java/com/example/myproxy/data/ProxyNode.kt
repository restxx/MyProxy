package com.example.myproxy.data

data class ProxyNode(
    val id: Int = 0,
    val name: String,
    val protocol: String,
    val server: String,
    val port: Int,
    val uuid: String,
    val security: String,
    val network: String,
    val host: String,
    val sni: String,
    val path: String,
    var latency: Int = -1,
    var selected: Boolean = false
)
