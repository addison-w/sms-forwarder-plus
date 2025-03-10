package com.smsforwarderplus.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smsforwarderplus.R

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    // Hardcoded version instead of using BuildConfig
    val appVersion = "1.0.0"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AboutSection(
            title = stringResource(R.string.version),
            content = appVersion
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AboutSection(
            title = stringResource(R.string.description),
            content = stringResource(R.string.app_description)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AboutSection(
            title = stringResource(R.string.features),
            content = stringResource(R.string.app_features)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AboutSection(
            title = stringResource(R.string.privacy),
            content = stringResource(R.string.privacy_policy)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AboutSection(
            title = stringResource(R.string.license),
            content = stringResource(R.string.license_info)
        )
    }
}

@Composable
fun AboutSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 