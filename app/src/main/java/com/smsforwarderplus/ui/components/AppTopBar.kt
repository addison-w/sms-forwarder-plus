package com.smsforwarderplus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.smsforwarderplus.R
import com.smsforwarderplus.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = when (currentRoute) {
                    Screen.Home.route -> stringResource(R.string.app_name)
                    Screen.Settings.route -> stringResource(R.string.settings)
                    Screen.About.route -> stringResource(R.string.about)
                    else -> stringResource(R.string.app_name)
                }
            )
        },
        actions = {
            if (currentRoute != Screen.Home.route) {
                IconButton(onClick = onNavigateToHome) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.app_name)
                    )
                }
            }
            
            if (currentRoute != Screen.Settings.route) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
            
            if (currentRoute != Screen.About.route) {
                IconButton(onClick = onNavigateToAbout) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.about)
                    )
                }
            }
        }
    )
} 