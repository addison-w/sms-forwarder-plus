package com.smsforwarderplus.utils

import android.content.Context
import android.telephony.SmsMessage
import com.smsforwarderplus.R
import com.smsforwarderplus.data.SMTPSettings
import com.smsforwarderplus.email.EmailService
import com.smsforwarderplus.receivers.SMSReceiver.CombinedSmsMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsUtils {
    
    /**
     * Format an SMS message for email
     * @param context The application context
     * @param sms The SMS message to format
     * @return A Pair containing the email subject and body
     */
    fun formatSmsForEmail(context: Context, sms: SmsMessage): Pair<String, String> {
        val sender = sms.originatingAddress ?: "Unknown"
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(sms.timestampMillis))
        val messageBody = sms.messageBody
        
        val subject = context.getString(R.string.email_subject, sender)
        val body = context.getString(R.string.email_body, sender, timestamp, messageBody)
        
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
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(sms.timestampMillis))
        val messageBody = sms.messageBody
        
        val subject = context.getString(R.string.email_subject, sender)
        val body = context.getString(R.string.email_body, sender, timestamp, messageBody)
        
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