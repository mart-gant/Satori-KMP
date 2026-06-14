package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.Routine
import com.gantlab.satori.db.RoutineTask
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.db.TaskCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

data class RoutineData(
    val routines: List<Routine>,
    val tasksByRoutine: Map<Long, List<RoutineTask>>,
    val recentCompletions: List<TaskCompletion>
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
