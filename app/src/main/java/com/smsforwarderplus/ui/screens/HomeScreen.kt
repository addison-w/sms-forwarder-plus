package com.smsforwarderplus.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.smsforwarderplus.R
import com.smsforwarderplus.data.SMTPSettings
import com.smsforwarderplus.utils.PermissionUtils

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen(
    settings: SMTPSettings?,
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Check if the app is ignoring battery optimizations
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(PermissionUtils.isIgnoringBatteryOptimizations(context))
    }
    
    // Update the battery optimization status when the screen is resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(TAG, "Lifecycle ON_RESUME - Updating battery optimization status")
                isIgnoringBatteryOptimizations = PermissionUtils.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Also check on initial composition
    LaunchedEffect(Unit) {
        Log.d(TAG, "Initial composition - Checking battery optimization status")
        isIgnoringBatteryOptimizations = PermissionUtils.isIgnoringBatteryOptimizations(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasSmsPermission) {
            PermissionCard(onRequestPermission)
        } else if (settings == null || settings.host.isEmpty() || settings.username.isEmpty() || 
                  settings.password.isEmpty() || settings.senderEmail.isEmpty() || 
                  settings.recipientEmail.isEmpty()) {
            SetupCard(onNavigateToSettings)
        } else {
            // Service Status Card - Now more prominent
            ServiceStatusCard(
                isServiceRunning = settings.isServiceEnabled,
                onStartService = onStartService,
                onStopService = onStopService
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Background Permission Card - Now below Service Status
            BackgroundPermissionCard(
                isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                onRequestDirectExemption = {
                    Log.d(TAG, "Direct exemption button clicked")
                    activity?.let {
                        PermissionUtils.requestBatteryOptimizationExemption(it)
                    } ?: run {
                        Log.e(TAG, "Activity is null, cannot request direct exemption")
                    }
                },
                onOpenBatterySettings = {
                    Log.d(TAG, "Battery settings button clicked")
                    PermissionUtils.openBatteryOptimizationSettings(context)
                }
            )
        }
    }
}

@Composable
fun BackgroundPermissionCard(
    isIgnoringBatteryOptimizations: Boolean,
    onRequestDirectExemption: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    // Less prominent card with lower elevation
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.background_permission),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.background_running),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = if (isIgnoringBatteryOptimizations) 
                        stringResource(R.string.background_enabled) 
                    else 
                        stringResource(R.string.background_disabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isIgnoringBatteryOptimizations) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
            
            if (!isIgnoringBatteryOptimizations) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.background_explanation),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestDirectExemption,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.enable_background),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onOpenBatterySettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.open_battery_settings),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.permission_required),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.permission_explanation),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
fun SetupCard(onNavigateToSettings: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup Required",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Please configure your SMTP settings to start forwarding SMS messages.",
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.settings),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ServiceStatusCard(
    isServiceRunning: Boolean,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    // More prominent card with higher elevation and larger size
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.service_status),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isServiceRunning) 
                    stringResource(R.string.service_running) 
                else 
                    stringResource(R.string.service_stopped),
                style = MaterialTheme.typography.titleLarge,
                color = if (isServiceRunning) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Switch(
                checked = isServiceRunning,
                onCheckedChange = { isChecked ->
                    if (isChecked) onStartService() else onStopService()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isServiceRunning) {
                OutlinedButton(
                    onClick = onStopService,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.stop_service),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Button(
                    onClick = onStartService,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.start_service),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
} 