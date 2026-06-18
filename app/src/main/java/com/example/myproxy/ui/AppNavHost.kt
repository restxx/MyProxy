package com.example.myproxy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myproxy.viewmodel.MainUiState

private const val ROUTE_HOME = "home"

@Composable
fun AppNavHost(
    navController: NavHostController,
    uiState: MainUiState,
    onUpdateNodes: () -> Unit,
    onSpeedTestAll: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSelectNode: (Int) -> Unit,
    onSortByLatency: () -> Unit
) {
    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeScreen(
                uiState = uiState,
                onUpdateNodes = onUpdateNodes,
                onSpeedTestAll = onSpeedTestAll,
                onConnect = onConnect,
                onDisconnect = onDisconnect,
                onSelectNode = onSelectNode,
                onSortByLatency = onSortByLatency
            )
        }
    }
}
