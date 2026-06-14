package com.gantlab.satori.domain.usecase

import com.gantlab.satori.network.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncDataUseCase(
    private val syncManager: SyncManager
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        syncManager.syncAll()
    }
}
