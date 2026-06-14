package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.notifications.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateRoutineTaskUseCase(
    private val repository: SatoriRepository,
    private val notifications: NotificationManager?
) {
    suspend fun addRoutine(title: String, icon: String?) = withContext(Dispatchers.Default) {
        repository.createRoutine(title, icon)
    }

    suspend fun deleteRoutine(id: Long) = withContext(Dispatchers.Default) {
        repository.deleteRoutine(id)
    }

    suspend fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) = withContext(Dispatchers.Default) {
        repository.updateRoutine(id, title, icon, isActive)
    }

    suspend fun addTask(routineId: Long, name: String, time: String?) = withContext(Dispatchers.Default) {
        repository.addTaskToRoutine(routineId, name, time)
        if (time != null) {
            val newTask = repository.getTasksForRoutine(routineId)
                .firstOrNull { it.taskName == name && it.scheduledTime == time }
            newTask?.let { 
                notifications?.scheduleTaskNotification(it.id, it.taskName, time)
            }
        }
    }

    suspend fun updateTaskDetails(taskId: Long, name: String, time: String?) = withContext(Dispatchers.Default) {
        repository.updateTaskDetails(taskId, name, time)
        if (time != null) {
            notifications?.scheduleTaskNotification(taskId, name, time)
        } else {
            notifications?.cancelTaskNotification(taskId)
        }
    }

    suspend fun updateCompletion(taskId: Long, isCompleted: Boolean) = withContext(Dispatchers.Default) {
        repository.updateTaskCompletion(taskId, isCompleted)
    }
}
