package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import javax.inject.Inject

open class OpenBoxUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    open suspend operator fun invoke(papeletaCode: String): Result<Box> {
        if (papeletaCode.isBlank()) {
            return Result.failure(Exception("Código da papeleta inválido."))
        }
        return repository.openBox(papeletaCode.trim())
    }
}
