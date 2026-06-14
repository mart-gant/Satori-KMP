package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExportDataUseCase(private val repository: SatoriRepository) {
    suspend operator fun invoke(): String = withContext(Dispatchers.Default) {
        repository.exportAllDataToCsv()
    }
}
