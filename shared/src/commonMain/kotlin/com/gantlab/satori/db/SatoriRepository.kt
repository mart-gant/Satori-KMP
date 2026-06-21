package com.gantlab.satori.db

interface SatoriRepository : 
    ReactionRepository, 
    MoodRepository, 
    ChallengeRepository, 
    RoutineRepository, 
    AssessmentRepository,
    ScenarioRepository {
    
    suspend fun clearAllData()
    suspend fun exportAllDataToCsv(): String
}
