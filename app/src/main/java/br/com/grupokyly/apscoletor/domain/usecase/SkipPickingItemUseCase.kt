package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import javax.inject.Inject

class SkipPickingItemUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    suspend operator fun invoke(
        pickingItemId: Long,
        boxId: Long,
        reason: SkipReason
    ): Result<ScanResult.ItemSkipped> {
        if (pickingItemId <= 0 || boxId <= 0) {
            return Result.failure(IllegalArgumentException("IDs inválidos para pular item."))
        }
        return repository.skipItem(pickingItemId, boxId, reason)
    }
}
