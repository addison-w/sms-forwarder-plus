package com.smsforwarderplus.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.smsforwarderplus.data.PreferencesManager
import com.smsforwarderplus.services.SMSForwarderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferencesManager = PreferencesManager(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val settings = preferencesManager.smtpSettingsFlow.first()
                
                if (settings.isServiceEnabled) {
                    val serviceIntent = Intent(context, SMSForwarderService::class.java)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
} 