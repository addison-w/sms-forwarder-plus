package com.smsforwarderplus.data

data class SMTPSettings(
    val host: String = "",
    val port: Int = 587,
    val username: String = "",
    val password: String = "",
    val senderEmail: String = "",
    val recipientEmail: String = "",
    val useSSL: Boolean = false,
    val isServiceEnabled: Boolean = false
) 