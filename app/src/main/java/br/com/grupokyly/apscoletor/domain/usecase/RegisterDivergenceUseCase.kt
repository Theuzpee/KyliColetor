package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import javax.inject.Inject

class RegisterDivergenceUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    suspend operator fun invoke(
        pickingItemId: Long,
        boxId: Long,
        barcode: String?,
        reason: SkipReason,
        evidencePhotoUrl: String? = null
    ): Result<Unit> {
        if (pickingItemId <= 0 || boxId <= 0) {
            return Result.failure(IllegalArgumentException("IDs inválidos para registrar divergência."))
        }
        return repository.registerDivergence(pickingItemId, boxId, barcode, reason, evidencePhotoUrl)
    }
}
