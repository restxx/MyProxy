package com.example.myproxy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
    val latency: Int = -1,
    val selected: Boolean = false
)

fun NodeEntity.toModel(): ProxyNode = ProxyNode(
    id = id,
    name = name,
    protocol = protocol,
    server = server,
    port = port,
    uuid = uuid,
    security = security,
    network = network,
    host = host,
    sni = sni,
    path = path,
    latency = latency,
    selected = selected
)

fun ProxyNode.toEntity(): NodeEntity = NodeEntity(
    id = id,
    name = name,
    protocol = protocol,
    server = server,
    port = port,
    uuid = uuid,
    security = security,
    network = network,
    host = host,
    sni = sni,
    path = path,
    latency = latency,
    selected = selected
)
