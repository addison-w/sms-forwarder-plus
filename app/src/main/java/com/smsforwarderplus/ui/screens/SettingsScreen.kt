package com.smsforwarderplus.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smsforwarderplus.R
import com.smsforwarderplus.data.PreferencesManager
import com.smsforwarderplus.data.SMTPSettings
import com.smsforwarderplus.email.EmailService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars

private const val TAG = "SettingsScreen"
private const val SSL_PORT = "465"
private const val NON_SSL_PORT = "587"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    settings: SMTPSettings?,
    onConnectionResult: (String) -> Unit,
    connectionResult: String?,
    onConnectionResultShown: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val emailService = remember { EmailService() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Focus requesters for each field
    val hostFocusRequester = remember { FocusRequester() }
    val portFocusRequester = remember { FocusRequester() }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val senderEmailFocusRequester = remember { FocusRequester() }
    val recipientEmailFocusRequester = remember { FocusRequester() }
    
    var host by remember { mutableStateOf(settings?.host ?: "") }
    var port by remember { mutableStateOf(settings?.port?.toString() ?: NON_SSL_PORT) }
    var username by remember { mutableStateOf(settings?.username ?: "") }
    var password by remember { mutableStateOf(settings?.password ?: "") }
    var senderEmail by remember { mutableStateOf(settings?.senderEmail ?: "") }
    var recipientEmail by remember { mutableStateOf(settings?.recipientEmail ?: "") }
    var useSSL by remember { mutableStateOf(settings?.useSSL ?: false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Update port when SSL toggle changes
    LaunchedEffect(useSSL) {
        // Only auto-update port if it's one of the standard ports
        if (port == SSL_PORT || port == NON_SSL_PORT) {
            port = if (useSSL) SSL_PORT else NON_SSL_PORT
            Log.d(TAG, "Auto-updated port to $port based on SSL setting: $useSSL")
        }
    }
    
    var isTesting by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Show result dialog when there's a connection result
    if (connectionResult != null) {
        ResultDialog(
            message = connectionResult,
            onDismiss = {
                onConnectionResultShown()
            }
        )
    }
    
    // Reset UI state when settings change
    LaunchedEffect(settings) {
        if (settings != null) {
            host = settings.host
            port = settings.port.toString()
            username = settings.username
            password = settings.password
            senderEmail = settings.senderEmail
            recipientEmail = settings.recipientEmail
            useSSL = settings.useSSL
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Dismiss keyboard when clicking outside input fields
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // Add padding for the keyboard
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.smtp_settings),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Server Settings Section
            Text(
                text = "Server Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )
            
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text(stringResource(R.string.smtp_host)) },
                placeholder = { Text("smtp.gmail.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(hostFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                // Scroll to this field when focused
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (host.isNotEmpty()) {
                        IconButton(onClick = { host = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text(stringResource(R.string.smtp_port)) },
                    placeholder = { Text(if (useSSL) SSL_PORT else NON_SSL_PORT) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(portFocusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    // Scroll to this field when focused
                                    val scrollPosition = scrollState.maxValue * 0.2f
                                    scrollState.animateScrollTo(scrollPosition.toInt())
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = useSSL,
                        onCheckedChange = { 
                            useSSL = it
                            // Port will be updated by the LaunchedEffect
                        }
                    )
                    
                    Text(
                        text = stringResource(R.string.use_ssl),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Authentication Section
            Text(
                text = "Authentication",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Start
            )
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.smtp_username)) },
                placeholder = { Text("your.email@gmail.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(usernameFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                // Scroll to this field when focused
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (username.isNotEmpty()) {
                        IconButton(onClick = { username = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.smtp_password)) },
                placeholder = { Text("password or app password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                // Scroll to this field when focused
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Check else Icons.Default.Lock,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Email Settings Section
            Text(
                text = "Email Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Start
            )
            
            OutlinedTextField(
                value = senderEmail,
                onValueChange = { senderEmail = it },
                label = { Text(stringResource(R.string.sender_email)) },
                placeholder = { Text("your.email@gmail.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(senderEmailFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                // Calculate approximate position to scroll to
                                val scrollPosition = scrollState.maxValue * 0.7f
                                scrollState.animateScrollTo(scrollPosition.toInt())
                            }
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (senderEmail.isNotEmpty()) {
                        IconButton(onClick = { senderEmail = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = recipientEmail,
                onValueChange = { recipientEmail = it },
                label = { Text(stringResource(R.string.recipient_email)) },
                placeholder = { Text("recipient@example.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(recipientEmailFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                // Scroll to bottom when this field is focused
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (recipientEmail.isNotEmpty()) {
                        IconButton(onClick = { recipientEmail = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Hide keyboard when button is clicked
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        
                        Log.d(TAG, "Test connection button clicked")
                        isTesting = true
                        
                        // Validate inputs
                        if (host.isBlank()) {
                            onConnectionResult("SMTP host cannot be empty")
                            isTesting = false
                            return@OutlinedButton
                        }
                        
                        if (username.isBlank()) {
                            onConnectionResult("Username cannot be empty")
                            isTesting = false
                            return@OutlinedButton
                        }
                        
                        if (password.isBlank()) {
                            onConnectionResult("Password cannot be empty")
                            isTesting = false
                            return@OutlinedButton
                        }
                        
                        val portInt = port.toIntOrNull()
                        if (portInt == null) {
                            onConnectionResult("Invalid port number")
                            isTesting = false
                            return@OutlinedButton
                        }
                        
                        val testSettings = SMTPSettings(
                            host = host,
                            port = portInt,
                            username = username,
                            password = password,
                            senderEmail = senderEmail,
                            recipientEmail = recipientEmail,
                            useSSL = useSSL
                        )
                        
                        coroutineScope.launch {
                            try {
                                Log.d(TAG, "Testing connection with settings: $testSettings")
                                val result = withContext(Dispatchers.IO) {
                                    emailService.testConnection(testSettings)
                                }
                                isTesting = false
                                
                                when (result) {
                                    is EmailService.EmailResult.Success -> {
                                        Log.d(TAG, "Connection test successful")
                                        onConnectionResult(context.getString(R.string.connection_success))
                                    }
                                    is EmailService.EmailResult.Error -> {
                                        Log.e(TAG, "Connection test failed: ${result.message}")
                                        onConnectionResult(
                                            context.getString(
                                                R.string.connection_failed,
                                                result.message
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Exception during connection test: ${e.message}", e)
                                isTesting = false
                                onConnectionResult(
                                    context.getString(
                                        R.string.connection_failed,
                                        e.message ?: "Unknown error"
                                    )
                                )
                            }
                        }
                    },
                    enabled = !isTesting && !isSaving,
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = if (isTesting) "Testing..." else stringResource(R.string.test_connection))
                }
                
                Button(
                    onClick = {
                        // Hide keyboard when button is clicked
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        
                        Log.d(TAG, "Save settings button clicked")
                        isSaving = true
                        
                        // Validate inputs
                        if (host.isBlank()) {
                            onConnectionResult("SMTP host cannot be empty")
                            isSaving = false
                            return@Button
                        }
                        
                        if (username.isBlank()) {
                            onConnectionResult("Username cannot be empty")
                            isSaving = false
                            return@Button
                        }
                        
                        if (password.isBlank()) {
                            onConnectionResult("Password cannot be empty")
                            isSaving = false
                            return@Button
                        }
                        
                        if (senderEmail.isBlank()) {
                            onConnectionResult("Sender email cannot be empty")
                            isSaving = false
                            return@Button
                        }
                        
                        if (recipientEmail.isBlank()) {
                            onConnectionResult("Recipient email cannot be empty")
                            isSaving = false
                            return@Button
                        }
                        
                        val portInt = port.toIntOrNull()
                        if (portInt == null) {
                            onConnectionResult("Invalid port number")
                            isSaving = false
                            return@Button
                        }
                        
                        val newSettings = SMTPSettings(
                            host = host,
                            port = portInt,
                            username = username,
                            password = password,
                            senderEmail = senderEmail,
                            recipientEmail = recipientEmail,
                            useSSL = useSSL,
                            isServiceEnabled = settings?.isServiceEnabled ?: false
                        )
                        
                        coroutineScope.launch {
                            try {
                                Log.d(TAG, "Saving settings: $newSettings")
                                withContext(Dispatchers.IO) {
                                    preferencesManager.updateSMTPSettings(newSettings)
                                }
                                Log.d(TAG, "Settings saved successfully")
                                onConnectionResult(context.getString(R.string.settings_saved))
                            } catch (e: Exception) {
                                Log.e(TAG, "Error saving settings: ${e.message}", e)
                                onConnectionResult(context.getString(R.string.settings_error))
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isTesting && !isSaving,
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = if (isSaving) "Saving..." else stringResource(R.string.save_settings))
                }
            }
            
            // Add extra space at the bottom to ensure the buttons are visible above the keyboard
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ResultDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK")
                }
            }
        }
    }
} 