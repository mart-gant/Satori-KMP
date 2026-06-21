package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    onLogin: (String) -> Unit,
    onRegister: (String) -> Unit,
    onBack: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "Logowanie" else "Rejestracja") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isLoginMode) "Witaj ponownie!" else "Utwórz konto",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Twoje dane będą bezpiecznie synchronizowane z chmurą.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Użytkownik") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isLoginMode) {
                        onLogin(password)
                    } else {
                        onRegister(password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(if (isLoginMode) "Zaloguj się" else "Zarejestruj się")
            }

            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(if (isLoginMode) "Nie masz konta? Zarejestruj się" else "Masz już konto? Zaloguj się")
            }
        }
    }
}
