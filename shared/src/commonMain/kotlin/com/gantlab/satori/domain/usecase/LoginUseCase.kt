package com.gantlab.satori.domain.usecase

import com.gantlab.satori.network.AuthRequest
import com.gantlab.satori.network.AuthResponse
import com.gantlab.satori.network.SatoriApiService
import com.gantlab.satori.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginUseCase(
    private val api: SatoriApiService?,
    private val settings: SettingsManager,
    private val syncDataUseCase: SyncDataUseCase
) {
    suspend operator fun invoke(username: String, password: String): Boolean = withContext(Dispatchers.Default) {
        val service = api ?: return@withContext false
        
        try {
            val response = service.login(AuthRequest(username, password))
            if (response != null) {
                settings.authToken = response.token
                settings.nickname = response.username
                syncDataUseCase()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
