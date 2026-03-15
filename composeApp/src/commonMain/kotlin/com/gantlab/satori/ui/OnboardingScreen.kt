package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onComplete: (String) -> Unit) {
    var nickname by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Witaj w Satori", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Satori pomaga monitorować Twój stan psychofizyczny poprzez proste testy reakcji.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Twoje imię lub pseudonim") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { if (nickname.isNotBlank()) onComplete(nickname) },
            enabled = nickname.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Zaczynamy!")
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Wszystkie Twoje dane są przechowywane lokalnie na urządzeniu.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
