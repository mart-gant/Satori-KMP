package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val license: String,
    val description: String
)

val libraries = listOf(
    OpenSourceLibrary("Kotlin Multiplatform", "JetBrains", "Apache 2.0", "The core language and multiplatform framework."),
    OpenSourceLibrary("Compose Multiplatform", "JetBrains", "Apache 2.0", "Declarative UI framework for multiple platforms."),
    OpenSourceLibrary("SQLDelight", "Cash App", "Apache 2.0", "Generates typesafe Kotlin APIs from your SQL statements."),
    OpenSourceLibrary("Koin", "InsertKoin.io", "Apache 2.0", "A pragmatic lightweight dependency injection framework."),
    OpenSourceLibrary("Ktor", "JetBrains", "Apache 2.0", "Asynchronous client and server framework for Kotlin."),
    OpenSourceLibrary("KotlinX Coroutines", "JetBrains", "Apache 2.0", "Library support for Kotlin coroutines."),
    OpenSourceLibrary("KotlinX DateTime", "JetBrains", "Apache 2.0", "A multiplatform Kotlin library for working with date and time."),
    OpenSourceLibrary("Multiplatform Settings", "Russell Wolf", "Apache 2.0", "A Kotlin Multiplatform library for saving simple key-value data.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(Res.string.app_full_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.version_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.oss_description),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            items(libraries) { lib ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = lib.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.by_author).replace("%s", lib.author),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lib.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.license_label).replace("%s", lib.license),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.copyright),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }
    }
}
