package com.example.myproxy.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myproxy.data.NodeDao
import com.example.myproxy.data.toEntity
import com.example.myproxy.data.toModel
import com.example.myproxy.network.SubscriptionRepository
import com.example.myproxy.speedtest.SpeedTestManager
import com.example.myproxy.vpn.MyVpnService
import com.example.myproxy.xray.XrayConfigBuilder
import com.example.myproxy.xray.XrayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val dao: NodeDao,
    private val repository: SubscriptionRepository,
    private val speedTestManager: SpeedTestManager,
    private val appContext: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val xrayManager = XrayManager(appContext)

    init {
        observeNodes()
    }

    private fun observeNodes() {
        viewModelScope.launch {
            dao.observeAllNodes().collectLatest { entities ->
                val nodes = entities.map { it.toModel() }
                val current = nodes.firstOrNull { it.selected }
                _uiState.value = _uiState.value.copy(
                    nodes = nodes,
                    currentNode = current
                )
            }
        }
    }

    fun updateNodes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusText = "更新节点中...")
            runCatching {
                val nodes = repository.fetchNodes()
                dao.replaceAll(nodes.map { it.toEntity() })
                nodes.size
            }.onFailure {
                _uiState.value = _uiState.value.copy(statusText = "更新失败")
            }.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    statusText = if (count > 0) "节点已更新: $count 个" else "未解析到可用节点"
                )
                ensureFirstNodeSelected()
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun ensureFirstNodeSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val selected = dao.getSelectedNode()
            if (selected == null) {
                val all = dao.getAllNodes()
                val first = all.firstOrNull() ?: return@launch
                dao.clearSelected()
                dao.setSelectedById(first.id)
            }
        }
    }

    fun speedTestAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusText = "测速中...")
            val currentNodes = uiState.value.nodes
            currentNodes.forEach { node ->
                val result = speedTestManager.testNode(node)
                dao.updateLatency(node.id, result)
            }
            sortByLatency()
            _uiState.value = _uiState.value.copy(isLoading = false, statusText = "测速完成")
        }
    }

    fun sortByLatency() {
        val sorted = uiState.value.nodes.sortedWith(
            compareBy(
                { if (it.latency < 0) Int.MAX_VALUE else it.latency },
                { it.name }
            )
        )
        _uiState.value = _uiState.value.copy(nodes = sorted)
    }

    fun selectNode(nodeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearSelected()
            dao.setSelectedById(nodeId)
        }
    }

    fun connect() {
        viewModelScope.launch {
            val node = uiState.value.currentNode ?: run {
                _uiState.value = _uiState.value.copy(statusText = "请先选择节点")
                return@launch
            }

            val config = XrayConfigBuilder.build(node)
            val configFile = xrayManager.writeConfig(config)
            val started = xrayManager.start(configFile)
            if (!started) {
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    statusText = "连接失败: Xray 内核未就绪"
                )
                return@launch
            }

            val intent = Intent(appContext, MyVpnService::class.java).apply {
                action = MyVpnService.ACTION_CONNECT
            }
            appContext.startService(intent)
            _uiState.value = _uiState.value.copy(
                isConnected = false,
                statusText = "连接流程已启动，请观察通知状态"
            )
        }
    }

    fun onVpnPermissionDenied() {
        _uiState.value = _uiState.value.copy(statusText = "VPN 授权被拒绝")
    }

    fun disconnect() {
        val intent = Intent(appContext, MyVpnService::class.java).apply {
            action = MyVpnService.ACTION_DISCONNECT
        }
        appContext.startService(intent)
        xrayManager.stop()
        _uiState.value = _uiState.value.copy(isConnected = false, statusText = "未连接")
    }
}

class MainViewModelFactory(
    private val dao: NodeDao,
    private val repository: SubscriptionRepository,
    private val speedTestManager: SpeedTestManager,
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(dao, repository, speedTestManager, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
