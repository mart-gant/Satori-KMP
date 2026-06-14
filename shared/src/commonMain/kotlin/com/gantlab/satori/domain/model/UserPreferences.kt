package com.gantlab.satori.domain.model

data class UserPreferences(
    val nickname: String,
    val isOnboardingCompleted: Boolean,
    val highContrast: Boolean,
    val largeFont: Boolean,
    val animationsEnabled: Boolean,
    val language: String,
    val aiConsentGranted: Boolean
)
