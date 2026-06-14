package com.gantlab.satori.domain.model

data class Reaction(
    val id: Long = 0,
    val timestamp: Long,
    val timeMs: Long,
    val synced: Boolean = false,
    val updatedAt: Long = 0
)
