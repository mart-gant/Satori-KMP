package com.gantlab.satori.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class SettingsManager(private val settings: Settings = Settings()) {
    
    var nickname: String
        get() = settings["nickname", ""]
        set(value) { settings["nickname"] = value }

    var isOnboardingCompleted: Boolean
        get() = settings["onboarding_completed", false]
        set(value) { settings["onboarding_completed"] = value }

    var highContrast: Boolean
        get() = settings["high_contrast", false]
        set(value) { settings["high_contrast"] = value }

    var largeFont: Boolean
        get() = settings["large_font", false]
        set(value) { settings["large_font"] = value }

    var animationsEnabled: Boolean
        get() = settings["animations_enabled", true]
        set(value) { settings["animations_enabled"] = value }
}
