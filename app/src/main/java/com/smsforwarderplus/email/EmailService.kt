package com.smsforwarderplus.email

import android.util.Log
import com.smsforwarderplus.data.SMTPSettings
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService {
    
    companion object {
        private const val TAG = "EmailService"
    }

    sealed class EmailResult {
        object Success : EmailResult()
        data class Error(val message: String) : EmailResult()
    }

    fun testConnection(settings: SMTPSettings): EmailResult {
        Log.d(TAG, "Testing connection to ${settings.host}:${settings.port}")
        
        if (settings.host.isEmpty()) {
            return EmailResult.Error("SMTP host cannot be empty")
        }
        
        if (settings.username.isEmpty()) {
            return EmailResult.Error("Username cannot be empty")
        }
        
        if (settings.password.isEmpty()) {
            return EmailResult.Error("Password cannot be empty")
        }
        
        return try {
            Log.d(TAG, "Creating session for test connection")
            val session = createSession(settings)
            
            Log.d(TAG, "Getting transport for ${if (settings.useSSL) "smtps" else "smtp"}")
            val transport = session.getTransport(if (settings.useSSL) "smtps" else "smtp")
            
            Log.d(TAG, "Connecting to server...")
            transport.connect(
                settings.host,
                settings.port,
                settings.username,
                settings.password
            )
            Log.d(TAG, "Connection successful")
            
            transport.close()
            Log.d(TAG, "Transport closed")
            
            EmailResult.Success
        } catch (e: MessagingException) {
            Log.e(TAG, "MessagingException during test: ${e.message}", e)
            EmailResult.Error(e.message ?: "Connection error")
        } catch (e: Exception) {
            Log.e(TAG, "Exception during test: ${e.message}", e)
            EmailResult.Error(e.message ?: "Unknown error")
        }
    }

    fun sendEmail(
        settings: SMTPSettings,
        subject: String,
        body: String
    ): EmailResult {
        Log.d(TAG, "Sending email with subject: $subject")
        
        if (settings.host.isEmpty()) {
            return EmailResult.Error("SMTP host cannot be empty")
        }
        
        if (settings.username.isEmpty()) {
            return EmailResult.Error("Username cannot be empty")
        }
        
        if (settings.password.isEmpty()) {
            return EmailResult.Error("Password cannot be empty")
        }
        
        if (settings.senderEmail.isEmpty()) {
            return EmailResult.Error("Sender email cannot be empty")
        }
        
        if (settings.recipientEmail.isEmpty()) {
            return EmailResult.Error("Recipient email cannot be empty")
        }
        
        return try {
            Log.d(TAG, "Creating session for sending email")
            val session = createSession(settings)
            
            Log.d(TAG, "Creating message")
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(settings.senderEmail))
                setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(settings.recipientEmail)
                )
                setSubject(subject)
                setText(body)
            }
            
            Log.d(TAG, "Sending message")
            Transport.send(message)
            Log.d(TAG, "Message sent successfully")
            
            EmailResult.Success
        } catch (e: MessagingException) {
            Log.e(TAG, "MessagingException during send: ${e.message}", e)
            EmailResult.Error(e.message ?: "Failed to send email")
        } catch (e: Exception) {
            Log.e(TAG, "Exception during send: ${e.message}", e)
            EmailResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun createSession(settings: SMTPSettings): Session {
        Log.d(TAG, "Creating mail session with SSL: ${settings.useSSL}")
        
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", settings.host)
            put("mail.smtp.port", settings.port.toString())
            
            if (settings.useSSL) {
                put("mail.smtp.socketFactory.port", settings.port.toString())
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.ssl.enable", "true")
            } else {
                put("mail.smtp.starttls.enable", "true")
            }
            
            // Add debug property
            put("mail.debug", "true")
        }
        
        Log.d(TAG, "Mail properties configured: $props")

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(settings.username, settings.password)
            }
        })
    }
} 