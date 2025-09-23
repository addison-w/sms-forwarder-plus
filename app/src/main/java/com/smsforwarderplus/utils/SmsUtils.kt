package com.smsforwarderplus.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsMessage
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.smsforwarderplus.R
import com.smsforwarderplus.data.SMTPSettings
import com.smsforwarderplus.email.EmailService
import com.smsforwarderplus.receivers.SMSReceiver.CombinedSmsMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsUtils {
    
    /**
     * Get the device's phone number for a specific subscription ID
     * @param context The application context
     * @param subscriptionId The subscription ID for dual SIM support
     * @return The device's phone number or "SIM1"/"SIM2"/etc. if not available
     */
    private fun getDevicePhoneNumber(context: Context, subscriptionId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID): String {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
                return "Unknown"
            }

            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                // Get phone number for specific subscription
                val subscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subscriptionId)
                if (subscriptionInfo != null) {
                    val phoneNumber = subscriptionInfo.number
                    if (!phoneNumber.isNullOrEmpty()) {
                        return phoneNumber
                    }
                    // Fall back to SIM slot display name if phone number is not available
                    val simSlotIndex = subscriptionInfo.simSlotIndex
                    return "SIM${simSlotIndex + 1}"
                }
            }

            // Fallback to default telephony manager
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.line1Number?.takeIf { it.isNotEmpty() } ?: "SIM1"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Format an SMS message for email
     * @param context The application context
     * @param sms The SMS message to format
     * @return A Pair containing the email subject and body
     */
    fun formatSmsForEmail(context: Context, sms: SmsMessage): Pair<String, String> {
        val sender = sms.originatingAddress ?: "Unknown"
        val recipient = getDevicePhoneNumber(context)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(sms.timestampMillis))
        val messageBody = sms.messageBody
        
        val subject = context.getString(R.string.email_subject, sender, recipient)
        val body = context.getString(R.string.email_body, sender, recipient, timestamp, messageBody)
        
        return Pair(subject, body)
    }
    
    /**
     * Format a combined SMS message for email
     * @param context The application context
     * @param sms The combined SMS message to format
     * @return A Pair containing the email subject and body
     */
    fun formatSmsForEmail(context: Context, sms: CombinedSmsMessage): Pair<String, String> {
        val sender = sms.originatingAddress
        val recipient = getDevicePhoneNumber(context, sms.subscriptionId)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(sms.timestampMillis))
        val messageBody = sms.messageBody

        val subject = context.getString(R.string.email_subject, sender, recipient)
        val body = context.getString(R.string.email_body, sender, recipient, timestamp, messageBody)

        return Pair(subject, body)
    }
    
    /**
     * Forward an SMS message to email
     * @param context The application context
     * @param sms The SMS message to forward
     * @param settings The SMTP settings to use
     * @return true if the email was sent successfully, false otherwise
     */
    suspend fun forwardSmsToEmail(
        context: Context, 
        sms: SmsMessage, 
        settings: SMTPSettings
    ): Boolean {
        if (!settings.isServiceEnabled || 
            settings.host.isEmpty() || 
            settings.username.isEmpty() || 
            settings.password.isEmpty() || 
            settings.senderEmail.isEmpty() || 
            settings.recipientEmail.isEmpty()) {
            return false
        }
        
        val emailService = EmailService()
        val (subject, body) = formatSmsForEmail(context, sms)
        
        return when (emailService.sendEmail(settings, subject, body)) {
            is EmailService.EmailResult.Success -> true
            is EmailService.EmailResult.Error -> false
        }
    }
    
    /**
     * Forward a combined SMS message to email
     * @param context The application context
     * @param sms The combined SMS message to forward
     * @param settings The SMTP settings to use
     * @return true if the email was sent successfully, false otherwise
     */
    suspend fun forwardSmsToEmail(
        context: Context, 
        sms: CombinedSmsMessage, 
        settings: SMTPSettings
    ): Boolean {
        if (!settings.isServiceEnabled || 
            settings.host.isEmpty() || 
            settings.username.isEmpty() || 
            settings.password.isEmpty() || 
            settings.senderEmail.isEmpty() || 
            settings.recipientEmail.isEmpty()) {
            return false
        }
        
        val emailService = EmailService()
        val (subject, body) = formatSmsForEmail(context, sms)
        
        return when (emailService.sendEmail(settings, subject, body)) {
            is EmailService.EmailResult.Success -> true
            is EmailService.EmailResult.Error -> false
        }
    }
} 