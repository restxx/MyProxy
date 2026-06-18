package com.example.myproxy

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.net.VpnService
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myproxy.data.AppDatabase
import com.example.myproxy.network.SubscriptionRepository
import com.example.myproxy.speedtest.SpeedTestManager
import androidx.navigation.compose.rememberNavController
import com.example.myproxy.ui.AppNavHost
import com.example.myproxy.ui.theme.MyProxyTheme
import com.example.myproxy.viewmodel.MainViewModel
import com.example.myproxy.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyProxyTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val appDb = remember(context) { AppDatabase.getInstance(context) }
    val repository = remember(context) { SubscriptionRepository() }
    val speedTestManager = remember { SpeedTestManager() }
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            dao = appDb.nodeDao(),
            repository = repository,
            speedTestManager = speedTestManager,
            appContext = context.applicationContext
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.connect()
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    val connectAction = {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            viewModel.connect()
        }
    }

    AppNavHost(
        navController = navController,
        uiState = uiState,
        onUpdateNodes = viewModel::updateNodes,
        onSpeedTestAll = viewModel::speedTestAll,
        onConnect = connectAction,
        onDisconnect = viewModel::disconnect,
        onSelectNode = viewModel::selectNode,
        onSortByLatency = viewModel::sortByLatency
    )
}