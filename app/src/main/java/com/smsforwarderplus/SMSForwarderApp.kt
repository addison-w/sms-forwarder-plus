package com.smsforwarderplus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class SMSForwarderApp : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "sms_forwarder_channel"
        private const val TAG = "SMSForwarderApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Creating notification channel")
                val name = getString(R.string.notification_channel_name)
                val descriptionText = getString(R.string.notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created successfully")
            } else {
                Log.d(TAG, "Skipping notification channel creation for API < 26")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel: ${e.message}", e)
        }
    }
} 