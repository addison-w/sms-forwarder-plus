package com.smsforwarderplus.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

object PermissionUtils {
    
    private const val TAG = "PermissionUtils"
    
    fun hasSmsPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    fun getSmsPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
    }
    
    fun getAllRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions.toTypedArray()
    }
    
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings: ${e.message}", e)
        }
    }
    
    /**
     * Checks if the app is ignoring battery optimizations (allowed to run in background)
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = context.packageName
            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
            Log.d(TAG, "Checking battery optimization status: isIgnoring=$isIgnoring")
            return isIgnoring
        } catch (e: Exception) {
            Log.e(TAG, "Error checking battery optimization status: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Opens the battery optimization settings screen for the app
     */
    fun requestBatteryOptimizationExemption(activity: Activity) {
        try {
            val packageName = activity.packageName
            Log.d(TAG, "Requesting battery optimization exemption for package: $packageName")
            
            // Direct approach using the specific intent
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            
            activity.startActivity(intent)
            Log.d(TAG, "Battery optimization exemption request intent sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request battery optimization exemption: ${e.message}", e)
            
            try {
                // Fallback to battery settings
                Log.d(TAG, "Trying fallback to battery settings")
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                activity.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open battery settings: ${e2.message}", e2)
                
                try {
                    // Last resort: open app settings
                    Log.d(TAG, "Trying last resort: open app settings")
                    openAppSettings(activity)
                } catch (e3: Exception) {
                    Log.e(TAG, "Failed to open app settings: ${e3.message}", e3)
                }
            }
        }
    }
    
    /**
     * Alternative method to open battery optimization settings
     * This can be used if the direct method doesn't work
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
            Log.d(TAG, "Opened battery optimization settings")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open battery optimization settings: ${e.message}", e)
            
            try {
                // Fallback
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                context.startActivity(intent)
                Log.d(TAG, "Opened battery saver settings as fallback")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open battery saver settings: ${e2.message}", e2)
            }
        }
    }
} 