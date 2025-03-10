package com.smsforwarderplus.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.smsforwarderplus.data.PreferencesManager
import com.smsforwarderplus.utils.SmsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SMSReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNotEmpty()) {
                for (smsMessage in messages) {
                    forwardSMS(context, smsMessage)
                }
            }
        }
    }

    private fun forwardSMS(context: Context, sms: SmsMessage) {
        val preferencesManager = PreferencesManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = preferencesManager.smtpSettingsFlow.first()
                val success = SmsUtils.forwardSmsToEmail(context, sms, settings)
                
                if (success) {
                    Log.d(TAG, "SMS forwarded successfully")
                } else {
                    Log.e(TAG, "Failed to forward SMS")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error forwarding SMS: ${e.message}", e)
            }
        }
    }
} 