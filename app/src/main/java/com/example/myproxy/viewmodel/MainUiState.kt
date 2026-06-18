package com.example.myproxy.viewmodel

import com.example.myproxy.data.ProxyNode

data class MainUiState(
    val nodes: List<ProxyNode> = emptyList(),
    val currentNode: ProxyNode? = null,
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val statusText: String = "未连接"
)
