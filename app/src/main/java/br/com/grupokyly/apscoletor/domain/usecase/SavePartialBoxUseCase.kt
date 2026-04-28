package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import javax.inject.Inject

open class SavePartialBoxUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    open suspend operator fun invoke(boxId: Long): Result<Box> {
        return repository.savePartialBox(boxId)
    }
}
