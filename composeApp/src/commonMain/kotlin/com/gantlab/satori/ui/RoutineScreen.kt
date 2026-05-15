package com.gantlab.satori.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gantlab.satori.db.Routine
import com.gantlab.satori.db.RoutineTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    routines: List<Routine>,
    tasks: Map<Long, List<RoutineTask>>,
    onAddRoutine: (String) -> Unit,
    onDeleteRoutine: (Long) -> Unit,
    onAddTask: (Long, String, String?) -> Unit,
    onUpdateTaskStatus: (Long, Boolean) -> Unit,
    onUpdateTaskName: (Long, String, String?) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newRoutineTitle by remember { mutableStateOf("") }
    
    var editingTask by remember { mutableStateOf<RoutineTask?>(null) }
    var editTaskName by remember { mutableStateOf("") }
    var editTaskTime by remember { mutableStateOf("") }

    if (editingTask != null) {
        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = { Text("Edytuj zadanie") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTaskName,
                        onValueChange = { editTaskName = it },
                        label = { Text("Nazwa zadania") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTaskTime,
                        onValueChange = { editTaskTime = it },
                        label = { Text("Godzina (np. 08:30)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingTask?.let { onUpdateTaskName(it.id, editTaskName, editTaskTime.ifBlank { null }) }
                    editingTask = null
                }) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(routines) { routine ->
                    RoutineItem(
                        routine = routine,
                        tasks = tasks[routine.id] ?: emptyList(),
                        onDelete = { onDeleteRoutine(routine.id) },
                        onAddTask = { name, time -> onAddTask(routine.id, name, time) },
                        onUpdateTaskStatus = onUpdateTaskStatus,
                        onLongPressTask = { task ->
                            editingTask = task
                            editTaskName = task.taskName
                            editTaskTime = task.scheduledTime ?: ""
                        }
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
    tasks: List<RoutineTask>,
    onDelete: () -> Unit,
    onAddTask: (String, String?) -> Unit,
    onUpdateTaskStatus: (Long, Boolean) -> Unit,
    onLongPressTask: (RoutineTask) -> Unit
) {
    var showAddTaskField by remember { mutableStateOf(false) }
    var newTaskName by remember { mutableStateOf("") }
    var newTaskTime by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(routine.title, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(Modifier.height(8.dp))

            tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onLongPressTask(task) },
                                onTap = { onUpdateTaskStatus(task.id, task.isCompletedToday == 0L) }
                            )
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompletedToday == 1L) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.isCompletedToday == 1L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = task.taskName + (task.scheduledTime?.let { " ($it)" } ?: ""),
                        style = if (task.isCompletedToday == 1L) 
                            MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        else 
                            MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (showAddTaskField) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newTaskName,
                            onValueChange = { newTaskName = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nazwa zadania") }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newTaskTime,
                            onValueChange = { newTaskTime = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Godzina (np. 08:30)") }
                        )
                        IconButton(onClick = {
                            if (newTaskName.isNotBlank()) {
                                onAddTask(newTaskName, newTaskTime.ifBlank { null })
                                newTaskName = ""
                                newTaskTime = ""
                                showAddTaskField = false
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj")
                        }
                    }
                }
            } else {
                TextButton(onClick = { showAddTaskField = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Dodaj zadanie")
                }
            }
        }
    }
}
