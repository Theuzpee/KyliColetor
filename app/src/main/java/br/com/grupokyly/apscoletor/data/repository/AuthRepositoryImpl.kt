package br.com.grupokyly.apscoletor.data.repository

import br.com.grupokyly.apscoletor.data.local.SessionManager
import br.com.grupokyly.apscoletor.data.remote.AuthRemoteDataSource
import br.com.grupokyly.apscoletor.domain.model.UserSession
import br.com.grupokyly.apscoletor.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionManager: SessionManager
) : AuthRepository {

    override val currentSession: Flow<UserSession?>
        get() = sessionManager.sessionFlow

    override suspend fun login(
        supervisorBarcode: String,
        operatorBarcode: String
    ): Result<UserSession> {
        val result = remoteDataSource.login(supervisorBarcode, operatorBarcode)
        
        return result.fold(
            onSuccess = { response ->
                sessionManager.saveSession(response)
                Result.success(
                    UserSession(
                        token = response.token,
                        operatorName = response.operatorName,
                        operatorCode = response.operatorCode,
                        supervisorName = response.supervisorName,
                        shift = response.shift
                    )
                )
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun isAuthenticated(): Boolean {
        return sessionManager.isSessionValid()
    }
}
