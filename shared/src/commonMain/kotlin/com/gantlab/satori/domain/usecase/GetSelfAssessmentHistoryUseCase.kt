package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.db.SelfAssessmentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSelfAssessmentHistoryUseCase(private val repository: SatoriRepository) {
    suspend operator fun invoke(): List<SelfAssessmentResult> = withContext(Dispatchers.Default) {
        repository.getSelfAssessmentHistory()
    }
}
