package com.gantlab.satori.network

import kotlinx.serialization.Serializable

@Serializable
data class MoodRequest(val moodScore: Long, val energyScore: Long, val note: String?)

@Serializable
data class MoodResponse(val id: Long, val timestamp: Long, val moodScore: Long, val energyScore: Long, val note: String?)
