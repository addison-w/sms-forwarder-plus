package com.smsforwarderplus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.smsforwarderplus.data.PreferencesManager
import com.smsforwarderplus.ui.SMSForwarderApp
import com.smsforwarderplus.ui.theme.SMSForwarderPlusTheme
import com.smsforwarderplus.utils.PermissionUtils
import com.smsforwarderplus.utils.ServiceUtils

class MainActivity : ComponentActivity() {
    
    private lateinit var preferencesManager: PreferencesManager
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        try {
            preferencesManager = PreferencesManager(this)
            
            setContent {
                SMSForwarderPlusTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val settings by preferencesManager.smtpSettingsFlow.collectAsState(initial = null)
                        var hasSmsPermission by remember {
                            mutableStateOf(PermissionUtils.hasSmsPermissions(this))
                        }
                        
                        val permissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            hasSmsPermission = PermissionUtils.hasSmsPermissions(this)
                            Log.d(TAG, "SMS permissions granted: $hasSmsPermission")
                        }
                        
                        LaunchedEffect(Unit) {
                            Log.d(TAG, "Requesting permissions")
                            permissionLauncher.launch(PermissionUtils.getAllRequiredPermissions())
                        }
                        
                        SMSForwarderApp(
                            settings = settings,
                            hasSmsPermission = hasSmsPermission,
                            onRequestPermission = {
                                Log.d(TAG, "User requested SMS permissions")
                                permissionLauncher.launch(PermissionUtils.getSmsPermissions())
                            },
                            onStartService = { 
                                Log.d(TAG, "User requested to start service")
                                startSMSForwarderService() 
                            },
                            onStopService = { 
                                Log.d(TAG, "User requested to stop service")
                                stopSMSForwarderService() 
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
        }
    }
    
    private fun startSMSForwarderService() {
        try {
            Log.d(TAG, "Starting SMS Forwarder Service from MainActivity")
            ServiceUtils.startSMSForwarderService(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service from MainActivity: ${e.message}", e)
        }
    }
    
    private fun stopSMSForwarderService() {
        try {
            Log.d(TAG, "Stopping SMS Forwarder Service from MainActivity")
            ServiceUtils.stopSMSForwarderService(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service from MainActivity: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        try {
            // Check if permissions have changed while app was in background
            if (PermissionUtils.hasSmsPermissions(this)) {
                Log.d(TAG, "SMS permissions are granted, checking service status")
                // If service should be running but isn't, restart it
                ServiceUtils.isServiceRunning(this) { isRunning ->
                    if (isRunning) {
                        Log.d(TAG, "Service should be running, starting it")
                        ServiceUtils.startSMSForwarderService(this)
                    } else {
                        Log.d(TAG, "Service should not be running")
                    }
                }
            } else {
                Log.d(TAG, "SMS permissions are not granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}", e)
        }
    }
} 