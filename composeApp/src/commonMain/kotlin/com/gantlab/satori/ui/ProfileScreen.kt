package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    nickname: String,
    highContrast: Boolean,
    largeFont: Boolean,
    animationsEnabled: Boolean,
    aiConsentGranted: Boolean,
    language: String,
    isLoggedIn: Boolean,
    onNicknameChange: (String) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onLargeFontChange: (Boolean) -> Unit,
    onAnimationsChange: (Boolean) -> Unit,
    onAiConsentChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToDebug: () -> Unit = {},
    onLogout: () -> Unit,
    onExportData: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text(stringResource(Res.string.nickname)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(stringResource(Res.string.accessibility), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.high_contrast))
                Spacer(Modifier.weight(1f))
                Switch(checked = highContrast, onCheckedChange = onHighContrastChange)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.large_font))
                Spacer(Modifier.weight(1f))
                Switch(checked = largeFont, onCheckedChange = onLargeFontChange)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.animations_enabled))
                Spacer(Modifier.weight(1f))
                Switch(checked = animationsEnabled, onCheckedChange = onAnimationsChange)
            }

            Spacer(Modifier.height(16.dp))

            Text("Prywatność i AI", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Zgoda na analizę AI")
                    Text(
                        "Pozwala na przesyłanie anonimowych danych do Gemini AI w celu generowania rekomendacji.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = aiConsentGranted, onCheckedChange = onAiConsentChange)
            }

            Spacer(Modifier.height(16.dp))
            
            Text(stringResource(Res.string.language_label), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = language == "pl",
                    onClick = { onLanguageChange("pl") },
                    label = { Text("Polski 🇵🇱") }
                )
                FilterChip(
                    selected = language == "en",
                    onClick = { onLanguageChange("en") },
                    label = { Text("English 🇬🇧") }
                )
            }

            Spacer(Modifier.height(24.dp))
            
            Text(stringResource(Res.string.cloud_sync_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            if (isLoggedIn) {
                Text(stringResource(Res.string.logged_in_as).replace("%s", nickname), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Text(stringResource(Res.string.logout))
                }
            } else {
                Text(stringResource(Res.string.local_data_only), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.login_register_btn))
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.export_csv_btn))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavigateToAbout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.about_app_btn))
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToDebug,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Panel Debuggera", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
