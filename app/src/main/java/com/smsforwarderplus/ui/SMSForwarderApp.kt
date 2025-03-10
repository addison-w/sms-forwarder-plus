package com.smsforwarderplus.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smsforwarderplus.data.SMTPSettings
import com.smsforwarderplus.ui.components.AppTopBar
import com.smsforwarderplus.ui.screens.AboutScreen
import com.smsforwarderplus.ui.screens.HomeScreen
import com.smsforwarderplus.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object About : Screen("about")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSForwarderApp(
    settings: SMTPSettings?,
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Screen.Home.route
    
    var showConnectionResult by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            AppTopBar(
                currentRoute = currentRoute,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    settings = settings,
                    hasSmsPermission = hasSmsPermission,
                    onRequestPermission = onRequestPermission,
                    onStartService = onStartService,
                    onStopService = onStopService,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    onConnectionResult = { result -> showConnectionResult = result },
                    connectionResult = showConnectionResult,
                    onConnectionResultShown = { showConnectionResult = null }
                )
            }
            
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
} 