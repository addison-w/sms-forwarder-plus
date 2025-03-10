package com.smsforwarderplus.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smsforwarderplus.R
import com.smsforwarderplus.data.SMTPSettings
import com.smsforwarderplus.ui.components.AppTopBar
import com.smsforwarderplus.ui.screens.AboutScreen
import com.smsforwarderplus.ui.screens.HomeScreen
import com.smsforwarderplus.ui.screens.SettingsScreen

sealed class Screen(val route: String, val icon: @Composable () -> Unit, val label: @Composable () -> Unit) {
    object Home : Screen(
        route = "home",
        icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
        label = { Text(stringResource(R.string.home)) }
    )
    object Settings : Screen(
        route = "settings",
        icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
        label = { Text(stringResource(R.string.settings)) }
    )
    object About : Screen(
        route = "about",
        icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
        label = { Text(stringResource(R.string.about)) }
    )
}

private val items = listOf(
    Screen.Home,
    Screen.Settings,
    Screen.About
)

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
    val currentDestination = currentBackStack?.destination
    val currentRoute = currentDestination?.route ?: Screen.Home.route
    
    var showConnectionResult by remember { mutableStateOf<String?>(null) }
    var navigationEnabled by remember { mutableStateOf(true) }
    
    // Monitor lifecycle to reset navigation state
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-enable navigation when the app is resumed
                navigationEnabled = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Function to navigate safely
    val navigateTo: (String) -> Unit = { route ->
        if (navigationEnabled) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentRoute = currentRoute,
                    onNavigateToHome = { 
                        if (navigationEnabled) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToSettings = { 
                        if (navigationEnabled) {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToAbout = { 
                        if (navigationEnabled) {
                            navController.navigate(Screen.About.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        
                        NavigationBarItem(
                            icon = screen.icon,
                            label = screen.label,
                            selected = selected,
                            onClick = {
                                // Only navigate if navigation is enabled
                                if (navigationEnabled) {
                                    // Clear any connection result when navigating
                                    if (showConnectionResult != null) {
                                        showConnectionResult = null
                                    }
                                    
                                    navController.navigate(screen.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            settings = settings,
                            hasSmsPermission = hasSmsPermission,
                            onRequestPermission = onRequestPermission,
                            onStartService = onStartService,
                            onStopService = onStopService,
                            onNavigateToSettings = { 
                                if (navigationEnabled) {
                                    navController.navigate(Screen.Settings.route)
                                }
                            }
                        )
                    }
                    
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            settings = settings,
                            onConnectionResult = { result -> 
                                // Temporarily disable navigation while showing result
                                navigationEnabled = false
                                showConnectionResult = result
                            },
                            connectionResult = showConnectionResult,
                            onConnectionResultShown = { 
                                showConnectionResult = null
                                // Re-enable navigation after a short delay
                                navigationEnabled = true
                            }
                        )
                    }
                    
                    composable(Screen.About.route) {
                        AboutScreen()
                    }
                }
            }
        }
    }
} 