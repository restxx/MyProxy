package com.example.myproxy.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myproxy.data.ProxyNode

@Composable
fun NodeListScreen(
    nodes: List<ProxyNode>,
    onNodeClick: (Int) -> Unit,
    onSortByLatency: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSortByLatency, modifier = Modifier.weight(1f)) {
                Text("按延迟排序")
            }
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("返回首页")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nodes, key = { it.id }) { node ->
                val isSelected = node.selected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNodeClick(node.id) }
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
                        if (isSelected) {
                            Text(text = "当前节点", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
