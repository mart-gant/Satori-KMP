package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.AssessmentRepository
import com.gantlab.satori.domain.model.DomainSelfAssessmentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSelfAssessmentHistoryUseCase(private val repository: AssessmentRepository) {
    suspend operator fun invoke(): List<DomainSelfAssessmentResult> = withContext(Dispatchers.Default) {
        repository.getSelfAssessmentHistory()
    }
}
