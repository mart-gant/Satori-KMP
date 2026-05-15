package com.gantlab.satori.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gantlab.satori.db.Routine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    routines: List<Routine>,
    onAddRoutine: (String) -> Unit,
    onDeleteRoutine: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newRoutineTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twoje Rutyny") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj rutynę")
            }
        }
    ) { padding ->
        if (routines.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Brak zdefiniowanych rutyn. Dodaj pierwszą!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(routines) { routine ->
                    RoutineItem(
                        routine = routine,
                        onDelete = { onDeleteRoutine(routine.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nowa Rutyna") },
                text = {
                    TextField(
                        value = newRoutineTitle,
                        onValueChange = { newRoutineTitle = it },
                        placeholder = { Text("np. Poranny spokój") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newRoutineTitle.isNotBlank()) {
                                onAddRoutine(newRoutineTitle)
                                newRoutineTitle = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}

@Composable
fun RoutineItem(
    routine: Routine,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(routine.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (routine.isActive == 1L) "Aktywna" else "Nieaktywna",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
