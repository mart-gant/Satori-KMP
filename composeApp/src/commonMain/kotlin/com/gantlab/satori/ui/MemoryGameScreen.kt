package com.gantlab.satori.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
    onResult: (score: Long) -> Unit,
    onBack: () -> Unit
) {
    val icons = listOf("🍎", "🍌", "🍒", "🍇", "🍉", "🍓", "🍍", "🥝")
    val cards = remember { (icons + icons).shuffled().toMutableStateList() }
    val revealed = remember { mutableStateListOf<Int>() }
    val matched = remember { mutableStateListOf<Int>() }
    var score by remember { mutableStateOf(0) }
    var moves by remember { mutableStateOf(0) }

    LaunchedEffect(revealed.size) {
        if (revealed.size == 2) {
            moves++
            delay(1000)
            if (cards[revealed[0]] == cards[revealed[1]]) {
                matched.add(revealed[0])
                matched.add(revealed[1])
                score += 10
            }
            revealed.clear()
            
            if (matched.size == cards.size) {
                onResult(score.toLong())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gra Pamięciowa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ruchy: $moves | Wynik: $score", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(cards) { index, icon ->
                    val isRevealed = revealed.contains(index) || matched.contains(index)
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable(enabled = !isRevealed && revealed.size < 2) {
                                revealed.add(index)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRevealed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isRevealed) {
                                Text(icon, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
            
            if (matched.size == cards.size) {
                Button(onClick = {
                    cards.clear()
                    cards.addAll((icons + icons).shuffled())
                    matched.clear()
                    revealed.clear()
                    moves = 0
                    score = 0
                }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Zagraj ponownie")
                }
            }
        }
    }
}
