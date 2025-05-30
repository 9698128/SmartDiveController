package com.divecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.divecontroller.ui.screens.*
import com.divecontroller.ui.theme.DiveControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiveControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiveControllerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveControllerApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination?.route) {
                            "dashboard" -> "Dashboard"
                            "site_detail/{siteId}" -> "Dettaglio Sito"
                            "alerts" -> "Avvisi"
                            "settings" -> "Impostazioni"
                            else -> "Dive Controller"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentDestination?.route == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    label = { Text("Avvisi") },
                    selected = currentDestination?.route == "alerts",
                    onClick = {
                        navController.navigate("alerts") {
                            popUpTo("dashboard")
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Impostazioni") },
                    selected = currentDestination?.route == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToSite = { siteId ->
                        navController.navigate("site_detail/$siteId")
                    }
                )
            }

            composable("site_detail/{siteId}") { backStackEntry ->
                val siteId = backStackEntry.arguments?.getString("siteId") ?: ""
                SiteDetailScreen(
                    siteId = siteId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("alerts") {
                AlertsScreen()
            }

            composable("settings") {
                SettingsScreen()
            }
        }
    }
}