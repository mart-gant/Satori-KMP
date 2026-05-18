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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    nickname: String,
    highContrast: Boolean,
    largeFont: Boolean,
    animationsEnabled: Boolean,
    isLoggedIn: Boolean,
    onNicknameChange: (String) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onLargeFontChange: (Boolean) -> Unit,
    onAnimationsChange: (Boolean) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onLogout: () -> Unit,
    onExportData: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Powrót"
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
                label = { Text("Pseudonim") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text("Dostępność", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tryb wysokiego kontrastu")
                Spacer(Modifier.weight(1f))
                Switch(checked = highContrast, onCheckedChange = onHighContrastChange)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Większa czcionka")
                Spacer(Modifier.weight(1f))
                Switch(checked = largeFont, onCheckedChange = onLargeFontChange)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Animacje włączone")
                Spacer(Modifier.weight(1f))
                Switch(checked = animationsEnabled, onCheckedChange = onAnimationsChange)
            }

            Spacer(Modifier.height(24.dp))
            
            Text("Chmura i Synchronizacja", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            if (isLoggedIn) {
                Text("Zalogowano jako: $nickname", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Text("Wyloguj się")
                }
            } else {
                Text("Twoje dane są zapisywane tylko lokalnie.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zaloguj lub Zarejestruj")
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eksportuj dane do CSV")
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
                Text("O aplikacji i licencje")
            }
        }
    }
}
