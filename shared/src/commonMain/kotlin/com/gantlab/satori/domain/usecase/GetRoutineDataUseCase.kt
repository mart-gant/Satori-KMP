package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

data class RoutineData(
    val routines: List<DomainRoutine>,
    val tasksByRoutine: Map<Long, List<DomainRoutineTask>>,
    val recentCompletions: List<DomainTaskCompletion>
)

class GetRoutineDataUseCase(private val repository: SatoriRepository) {
    
    suspend operator fun invoke(): RoutineData = withContext(Dispatchers.Default) {
        val routines = repository.getAllRoutines()
        val tasksMap = routines.associateBy({ it.id }) { 
            repository.getTasksForRoutine(it.id) 
        }
        
        val sevenDaysAgo = Clock.System.now()
            .minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        val completions = repository.getTaskCompletions(sevenDaysAgo)
        
        RoutineData(routines, tasksMap, completions)
    }
}
