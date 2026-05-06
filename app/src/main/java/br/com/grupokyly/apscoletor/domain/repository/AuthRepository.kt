package br.com.grupokyly.apscoletor.domain.repository

import br.com.grupokyly.apscoletor.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(
        supervisorBarcode: String,
        operatorBarcode: String
    ): Result<UserSession>
    
    suspend fun logout()
    
    suspend fun isAuthenticated(): Boolean
    
    val currentSession: Flow<UserSession?>
}
