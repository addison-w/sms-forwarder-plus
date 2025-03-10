package com.smsforwarderplus.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smsforwarderplus.MainActivity
import com.smsforwarderplus.R
import com.smsforwarderplus.SMSForwarderApp.Companion.NOTIFICATION_CHANNEL_ID
import com.smsforwarderplus.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SMSForwarderService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    
    companion object {
        private const val TAG = "SMSForwarderService"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        try {
            preferencesManager = PreferencesManager(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PreferencesManager: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        try {
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            
            serviceScope.launch {
                try {
                    preferencesManager.updateServiceStatus(true)
                    Log.d(TAG, "Service status updated to running")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating service status: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand: ${e.message}", e)
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        try {
            serviceScope.launch {
                try {
                    preferencesManager.updateServiceStatus(false)
                    Log.d(TAG, "Service status updated to stopped")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating service status on destroy: ${e.message}", e)
                }
            }
            serviceScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}", e)
        }
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        try {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification: ${e.message}", e)
            // Create a simple notification as fallback
            return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("SMS Forwarder")
                .setContentText("Service is running")
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
    }
} 