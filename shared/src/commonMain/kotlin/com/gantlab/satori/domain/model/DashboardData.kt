package com.gantlab.satori.domain.model

data class DashboardData(
    val recommendations: List<Recommendation>,
    val satoriScore: Int
)
