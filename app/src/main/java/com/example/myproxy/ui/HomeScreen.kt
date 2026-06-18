package com.example.myproxy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myproxy.data.ProxyNode
import com.example.myproxy.viewmodel.MainUiState

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onUpdateNodes: () -> Unit,
    onSpeedTestAll: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSelectNode: (Int) -> Unit,
    onSortByLatency: () -> Unit
) {
    val currentNode = uiState.currentNode
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "连接状态: ${if (uiState.isConnected) "已连接" else "未连接"}")
                Text(text = "当前节点: ${currentNode?.name ?: "未选择"}")
                Text(
                    text = "当前延迟: ${
                        if (currentNode == null || currentNode.latency < 0) "timeout"
                        else "${currentNode.latency} ms"
                    }"
                )
                Text(text = "状态: ${uiState.statusText}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onConnect, modifier = Modifier.weight(1f)) {
                Text("连接")
            }
            Button(onClick = onDisconnect, modifier = Modifier.weight(1f)) {
                Text("断开")
            }
        }

        Button(onClick = onUpdateNodes, modifier = Modifier.fillMaxWidth()) {
            Text("更新节点")
        }
        Button(onClick = onSpeedTestAll, modifier = Modifier.fillMaxWidth()) {
            Text("测速全部")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("节点列表", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onSortByLatency) {
                Text("按延迟排序")
            }
        }

        NodeList(
            nodes = uiState.nodes,
            onNodeClick = onSelectNode,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NodeList(
    nodes: List<ProxyNode>,
    onNodeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nodes, key = { it.id }) { node ->
            val isSelected = node.selected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = node.name,
                        style = if (isSelected) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        }
                    )
                    Text(
                        text = "延迟: ${if (node.latency < 0) "timeout" else "${node.latency} ms"}"
                    )
                    Text(text = "状态: ${if (node.latency < 0) "离线" else "在线"}")
                    Button(
                        onClick = { onNodeClick(node.id) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(if (isSelected) "当前节点" else "设为当前节点")
                    }
                }
            }
        }
    }
}
