package com.smsforwarderplus.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smsforwarderplus.data.PreferencesManager
import com.smsforwarderplus.services.SMSForwarderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ServiceUtils {
    
    private const val TAG = "ServiceUtils"
    
    fun startSMSForwarderService(context: Context) {
        try {
            Log.d(TAG, "Starting SMS Forwarder Service")
            val serviceIntent = Intent(context, SMSForwarderService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Using startForegroundService for Android O+")
                context.startForegroundService(serviceIntent)
            } else {
                Log.d(TAG, "Using startService for pre-Android O")
                context.startService(serviceIntent)
            }
            
            // Update preferences
            val preferencesManager = PreferencesManager(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    preferencesManager.updateServiceStatus(true)
                    Log.d(TAG, "Service status updated to running")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating service status: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service: ${e.message}", e)
        }
    }
    
    fun stopSMSForwarderService(context: Context) {
        try {
            Log.d(TAG, "Stopping SMS Forwarder Service")
            val serviceIntent = Intent(context, SMSForwarderService::class.java)
            context.stopService(serviceIntent)
            
            // Update preferences
            val preferencesManager = PreferencesManager(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    preferencesManager.updateServiceStatus(false)
                    Log.d(TAG, "Service status updated to stopped")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating service status: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service: ${e.message}", e)
        }
    }
    
    fun isServiceRunning(context: Context, callback: (Boolean) -> Unit) {
        try {
            val preferencesManager = PreferencesManager(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = preferencesManager.smtpSettingsFlow.first()
                    Log.d(TAG, "Service status checked: ${settings.isServiceEnabled}")
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(settings.isServiceEnabled)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking service status: ${e.message}", e)
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in isServiceRunning: ${e.message}", e)
            callback(false)
        }
    }
} 