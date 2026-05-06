package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import javax.inject.Inject

class SaveMultiFloorBoxUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    suspend operator fun invoke(boxId: Long): Result<Box> {
        if (boxId <= 0) {
            return Result.failure(Exception("ID de caixa inválido."))
        }
        return repository.saveMultiFloorBox(boxId)
    }
}
