package br.com.grupokyly.apscoletor.test.fake

import br.com.grupokyly.apscoletor.domain.model.UserSession
import br.com.grupokyly.apscoletor.data.remote.dto.LoginResponseDto

class FakeSessionManager {
    var savedSession: UserSession? = null
    var isValid: Boolean = true

    suspend fun saveSession(response: LoginResponseDto) {
        savedSession = fakeUserSession()
    }
    suspend fun clearSession() { savedSession = null }
    suspend fun getToken(): String? = savedSession?.token
    suspend fun isSessionValid(): Boolean = isValid
}
