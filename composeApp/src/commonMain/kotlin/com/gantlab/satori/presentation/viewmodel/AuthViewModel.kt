package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.domain.usecase.LoginUseCase
import com.gantlab.satori.domain.usecase.SyncDataUseCase
import com.gantlab.satori.network.AuthRequest
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.*
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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val KEY_USERNAME = "auth_username"

    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = settings.authToken != null))
    val uiState = _uiState.asStateFlow()

    // Login zapamiętany w SavedStateHandle
    val username = savedStateHandle.getStateFlow(KEY_USERNAME, "")

    fun updateUsername(name: String) {
        savedStateHandle[KEY_USERNAME] = name
    }

    fun login(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = loginUseCase(username.value, password)
            if (success) {
                _uiState.update { it.copy(isLoggedIn = true) }
            }
            onResult(success)
        }
    }

    fun register(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = api?.register(AuthRequest(username.value, password)) ?: false
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
