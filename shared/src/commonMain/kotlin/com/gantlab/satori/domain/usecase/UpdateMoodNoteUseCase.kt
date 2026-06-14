package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateMoodNoteUseCase(private val repository: SatoriRepository) {
    suspend operator fun invoke(id: Long, note: String) = withContext(Dispatchers.Default) {
        repository.updateMoodNote(id, note)
    }
}
