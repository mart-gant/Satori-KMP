package com.gantlab.satori.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gantlab.satori.db.MoodEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import satori.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodLoggingScreen(
    history: List<MoodEntry>,
    onSaveMood: (Long, Long, String) -> Unit,
    onUpdateNote: (Long, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedMood by remember { mutableLongStateOf(3L) }
    var selectedEnergy by remember { mutableLongStateOf(3L) }
    var note by remember { mutableStateOf("") }
    
    var editingEntry by remember { mutableStateOf<MoodEntry?>(null) }
    var editNoteText by remember { mutableStateOf("") }

    if (editingEntry != null) {
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text(stringResource(Res.string.edit_note)) },
            text = {
                OutlinedTextField(
                    value = editNoteText,
                    onValueChange = { editNoteText = it },
                    label = { Text(stringResource(Res.string.your_note)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editingEntry?.let { onUpdateNote(it.id, editNoteText) }
                    editingEntry = null
                }) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.mood_energy_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(stringResource(Res.string.how_feel_now), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                RatingSelector(
                    currentValue = selectedMood,
                    onValueChange = { selectedMood = it },
                    labels = listOf("😫", "😕", "😐", "🙂", "🤩")
                )
            }

            item {
                Text(stringResource(Res.string.how_energy_now), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                RatingSelector(
                    currentValue = selectedEnergy,
                    onValueChange = { selectedEnergy = it },
                    labels = listOf("😴", "🥱", "😐", "⚡", "🔥")
                )
            }

            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(Res.string.note_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Button(
                    onClick = {
                        onSaveMood(selectedMood, selectedEnergy, note)
                        note = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(stringResource(Res.string.save_state))
                }
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.recent_entries), style = MaterialTheme.typography.titleMedium)
            }

            items(history.take(10)) { entry ->
                MoodHistoryItem(entry, onEdit = {
                    editingEntry = entry
                    editNoteText = entry.note ?: ""
                })
            }
        }
    }
}

@Composable
fun RatingSelector(
    currentValue: Long,
    onValueChange: (Long) -> Unit,
    labels: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { index, label ->
            val value = (index + 1).toLong()
            val isSelected = value == currentValue
            
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onValueChange(value) },
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun MoodHistoryItem(entry: MoodEntry, onEdit: () -> Unit) {
    val date = Instant.fromEpochMilliseconds(entry.timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${date.hour}:${date.minute.toString().padStart(2, '0')} | ${date.dayOfMonth}.${date.monthNumber}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("M: ${getMoodEmoji(entry.moodScore)}", modifier = Modifier.padding(horizontal = 4.dp))
                    Text("E: ${getEnergyEmoji(entry.energyScore)}")
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.save), modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (!entry.note.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(entry.note!!, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

fun getMoodEmoji(score: Long) = when(score) {
    1L -> "😫"
    2L -> "😕"
    3L -> "😐"
    4L -> "🙂"
    5L -> "🤩"
    else -> "😐"
}

fun getEnergyEmoji(score: Long) = when(score) {
    1L -> "😴"
    2L -> "🥱"
    3L -> "😐"
    4L -> "⚡"
    5L -> "🔥"
    else -> "😐"
}
