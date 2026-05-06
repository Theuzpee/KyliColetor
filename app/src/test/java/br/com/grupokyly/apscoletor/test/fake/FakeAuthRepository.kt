package br.com.grupokyly.apscoletor.test.fake

import br.com.grupokyly.apscoletor.domain.model.UserSession
import br.com.grupokyly.apscoletor.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository : AuthRepository {
    var loginResult: Result<UserSession> = Result.success(fakeUserSession())
    var isAuthenticatedResult: Boolean = false

    override suspend fun login(
        supervisorBarcode: String,
        operatorBarcode: String
    ) = loginResult

    override suspend fun logout() {}
    override suspend fun isAuthenticated() = isAuthenticatedResult
    override val currentSession: Flow<UserSession?> = flowOf(null)
}
