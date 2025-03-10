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
                // Combine all message parts into a single SMS object
                val combinedSms = combineMessageParts(messages)
                // Forward the combined message
                forwardSMS(context, combinedSms)
            }
        }
    }

    /**
     * Combines multiple SmsMessage parts into a single SmsMessage with the complete text.
     * This ensures that multi-part SMS messages are treated as a single message.
     */
    private fun combineMessageParts(messageParts: Array<SmsMessage>): CombinedSmsMessage {
        // Use the first message part for metadata
        val firstPart = messageParts[0]
        val originatingAddress = firstPart.originatingAddress ?: "Unknown"
        val timestampMillis = firstPart.timestampMillis
        
        // Combine all message bodies
        val fullMessageBody = messageParts.joinToString("") { it.messageBody }
        
        Log.d(TAG, "Combined ${messageParts.size} message parts into a single message")
        
        return CombinedSmsMessage(
            originatingAddress = originatingAddress,
            messageBody = fullMessageBody,
            timestampMillis = timestampMillis
        )
    }

    private fun forwardSMS(context: Context, sms: CombinedSmsMessage) {
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
    
    /**
     * Data class to represent a combined SMS message from multiple parts
     */
    data class CombinedSmsMessage(
        val originatingAddress: String,
        val messageBody: String,
        val timestampMillis: Long
    )
} 