package com.gantlab.satori.settings

import com.gantlab.satori.domain.model.UserPreferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsManager(private val settings: Settings = Settings()) {
    
    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences() = UserPreferences(
        nickname = settings["nickname", ""],
        isOnboardingCompleted = settings["onboarding_completed", false],
        highContrast = settings["high_contrast", false],
        largeFont = settings["large_font", false],
        animationsEnabled = settings["animations_enabled", true],
        language = settings["language", "pl"],
        aiConsentGranted = settings["ai_consent_granted", false]
    )

    var nickname: String
        get() = settings["nickname", ""]
        set(value) { 
            settings["nickname"] = value
            _preferences.update { it.copy(nickname = value) }
        }

    var isOnboardingCompleted: Boolean
        get() = settings["onboarding_completed", false]
        set(value) { 
            settings["onboarding_completed"] = value
            _preferences.update { it.copy(isOnboardingCompleted = value) }
        }

    var highContrast: Boolean
        get() = settings["high_contrast", false]
        set(value) { 
            settings["high_contrast"] = value
            _preferences.update { it.copy(highContrast = value) }
        }

    var largeFont: Boolean
        get() = settings["large_font", false]
        set(value) { 
            settings["large_font"] = value
            _preferences.update { it.copy(largeFont = value) }
        }

    var animationsEnabled: Boolean
        get() = settings["animations_enabled", true]
        set(value) { 
            settings["animations_enabled"] = value
            _preferences.update { it.copy(animationsEnabled = value) }
        }

    var language: String
        get() = settings["language", "pl"]
        set(value) { 
            settings["language"] = value
            _preferences.update { it.copy(language = value) }
        }

    var aiConsentGranted: Boolean
        get() = settings["ai_consent_granted", false]
        set(value) { 
            settings["ai_consent_granted"] = value
            _preferences.update { it.copy(aiConsentGranted = value) }
        }

    var authToken: String?
        get() = settings.getStringOrNull("auth_token")
        set(value) { 
            if (value != null) settings.putString("auth_token", value)
            else settings.remove("auth_token")
        }
}
