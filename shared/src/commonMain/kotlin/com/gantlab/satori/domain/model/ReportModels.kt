package com.gantlab.satori.domain.model

data class MoodHeatmapCell(
    val dayIndex: Int, // 0-6
    val timeIndex: Int, // 0-2 (Rano, Dzień, Wieczór)
    val moodScore: Long?,
    val hasRoutineCompletion: Boolean
)

data class HourlyAnalysisPoint(
    val hour: Int,
    val averageMs: Long?,
    val heightFactor: Float // 0f - 1f
)

data class ReportsData(
    val heatmapCells: List<MoodHeatmapCell>,
    val hourlyAnalysis: List<HourlyAnalysisPoint>
)
