package com.gantlab.satori.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.domain.usecase.SyncDataUseCase

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncDataUseCase: SyncDataUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncDataUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
