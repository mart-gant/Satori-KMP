package com.gantlab.satori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gantlab.satori.Analytics
import com.gantlab.satori.domain.model.UserPreferences
import com.gantlab.satori.domain.usecase.ExportDataUseCase
import com.gantlab.satori.domain.usecase.SyncDataUseCase
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsManager,
    private val exportDataUseCase: ExportDataUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val analytics: Analytics
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    val uiState: StateFlow<UserPreferences> = settings.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = settings.preferences.value
        )

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncDataUseCase()
            _isSyncing.value = false
            analytics.logEvent("manual_sync_triggered")
        }
    }

    fun completeOnboarding() {
        settings.isOnboardingCompleted = true
        analytics.logEvent("onboarding_completed")
    }

    fun updateNickname(name: String) {
        settings.nickname = name
    }

    fun toggleHighContrast(enabled: Boolean) {
        settings.highContrast = enabled
    }

    fun toggleLargeFont(enabled: Boolean) {
        settings.largeFont = enabled
    }

    fun toggleAnimations(enabled: Boolean) {
        settings.animationsEnabled = enabled
    }

    fun updateLanguage(lang: String) {
        settings.language = lang
    }

    fun toggleAiConsent(enabled: Boolean) {
        settings.aiConsentGranted = enabled
    }

    suspend fun getExportData(): String = exportDataUseCase()
}
