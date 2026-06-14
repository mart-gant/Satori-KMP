package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.db.Routine
import com.gantlab.satori.db.RoutineTask
import com.gantlab.satori.db.TaskCompletion
import com.gantlab.satori.domain.usecase.GetRoutineDataUseCase
import com.gantlab.satori.domain.usecase.UpdateRoutineTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutineUiState(
    val routines: List<Routine> = emptyList(),
    val routineTasks: Map<Long, List<RoutineTask>> = emptyMap(),
    val taskCompletions: List<TaskCompletion> = emptyList(),
    val isLoading: Boolean = false
)

class RoutineViewModel(
    private val getRoutineDataUseCase: GetRoutineDataUseCase,
    private val updateRoutineTaskUseCase: UpdateRoutineTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = getRoutineDataUseCase()
            _uiState.update { 
                it.copy(
                    routines = data.routines,
                    routineTasks = data.tasksByRoutine,
                    taskCompletions = data.recentCompletions,
                    isLoading = false
                ) 
            }
        }
    }

    fun addRoutine(title: String, icon: String?) {
        viewModelScope.launch {
            updateRoutineTaskUseCase.addRoutine(title, icon)
            refreshData()
        }
    }

    fun updateRoutine(id: Long, title: String, icon: String?, isActive: Boolean) {
        viewModelScope.launch {
            updateRoutineTaskUseCase.updateRoutine(id, title, icon, isActive)
            refreshData()
        }
    }

    fun addTaskToRoutine(routineId: Long, name: String, time: String?) {
        viewModelScope.launch {
            updateRoutineTaskUseCase.addTask(routineId, name, time)
            refreshData()
        }
    }

    fun updateTaskDetails(taskId: Long, name: String, time: String?) {
        viewModelScope.launch {
            updateRoutineTaskUseCase.updateTaskDetails(taskId, name, time)
            refreshData()
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            updateRoutineTaskUseCase.deleteRoutine(id)
            refreshData()
        }
    }

    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            updateRoutineTaskUseCase.updateCompletion(taskId, isCompleted)
            refreshData()
        }
    }
}
