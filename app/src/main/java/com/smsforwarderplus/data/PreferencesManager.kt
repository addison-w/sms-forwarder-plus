package com.smsforwarderplus.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val SMTP_HOST = stringPreferencesKey("smtp_host")
        val SMTP_PORT = intPreferencesKey("smtp_port")
        val SMTP_USERNAME = stringPreferencesKey("smtp_username")
        val SMTP_PASSWORD = stringPreferencesKey("smtp_password")
        val SENDER_EMAIL = stringPreferencesKey("sender_email")
        val RECIPIENT_EMAIL = stringPreferencesKey("recipient_email")
        val USE_SSL = booleanPreferencesKey("use_ssl")
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
    }

    val smtpSettingsFlow: Flow<SMTPSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SMTPSettings(
                host = preferences[PreferencesKeys.SMTP_HOST] ?: "",
                port = preferences[PreferencesKeys.SMTP_PORT] ?: 587,
                username = preferences[PreferencesKeys.SMTP_USERNAME] ?: "",
                password = preferences[PreferencesKeys.SMTP_PASSWORD] ?: "",
                senderEmail = preferences[PreferencesKeys.SENDER_EMAIL] ?: "",
                recipientEmail = preferences[PreferencesKeys.RECIPIENT_EMAIL] ?: "",
                useSSL = preferences[PreferencesKeys.USE_SSL] ?: false,
                isServiceEnabled = preferences[PreferencesKeys.SERVICE_ENABLED] ?: false
            )
        }

    suspend fun updateSMTPSettings(settings: SMTPSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SMTP_HOST] = settings.host
            preferences[PreferencesKeys.SMTP_PORT] = settings.port
            preferences[PreferencesKeys.SMTP_USERNAME] = settings.username
            preferences[PreferencesKeys.SMTP_PASSWORD] = settings.password
            preferences[PreferencesKeys.SENDER_EMAIL] = settings.senderEmail
            preferences[PreferencesKeys.RECIPIENT_EMAIL] = settings.recipientEmail
            preferences[PreferencesKeys.USE_SSL] = settings.useSSL
        }
    }

    suspend fun updateServiceStatus(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVICE_ENABLED] = enabled
        }
    }
} 