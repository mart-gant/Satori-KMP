package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.domain.usecase.LoginUseCase
import com.gantlab.satori.domain.usecase.SyncDataUseCase
import com.gantlab.satori.network.AuthRequest
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isSyncing: Boolean = false
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val api: SatoriApiService? = null,
    private val settings: SettingsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = settings.authToken != null))
    val uiState = _uiState.asStateFlow()

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = loginUseCase(username, password)
            if (success) {
                _uiState.update { it.copy(isLoggedIn = true) }
            }
            onResult(success)
        }
    }

    fun register(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = api?.register(AuthRequest(username, password)) ?: false
            onResult(success)
        }
    }

    fun logout() {
        settings.authToken = null
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = settings.authToken ?: return@launch
            val success = api?.deleteAccount(token) ?: false
            if (success) {
                logout()
            }
            onResult(success)
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            syncDataUseCase()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}
