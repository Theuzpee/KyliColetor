package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import javax.inject.Inject

open class RegisterScanUseCase @Inject constructor(
    private val repository: PickingRepository
) {
    open suspend operator fun invoke(barcode: String, boxId: Long): Result<ScanResult> {
        if (barcode.isBlank()) {
            return Result.failure(Exception("Código de barras inválido."))
        }
        if (boxId <= 0) {
            return Result.failure(Exception("Caixa não identificada. Bipe a papeleta novamente."))
        }
        return repository.registerScan(barcode.trim(), boxId)
    }
}
