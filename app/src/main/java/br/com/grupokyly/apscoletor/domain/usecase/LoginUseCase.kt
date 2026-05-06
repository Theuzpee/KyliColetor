package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.UserSession
import br.com.grupokyly.apscoletor.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        supervisorBarcode: String,
        operatorBarcode: String
    ): Result<UserSession> {
        if (supervisorBarcode.isBlank()) {
            return Result.failure(Exception("Bipe o crachá do supervisor primeiro."))
        }
        if (operatorBarcode.isBlank()) {
            return Result.failure(Exception("Bipe o crachá do colaborador."))
        }
        if (supervisorBarcode == operatorBarcode) {
            return Result.failure(Exception("Supervisor e colaborador não podem ser o mesmo."))
        }

        return repository.login(supervisorBarcode, operatorBarcode)
    }
}
