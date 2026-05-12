package br.com.grupokyly.apscoletor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import br.com.grupokyly.apscoletor.data.remote.dto.LoginResponseDto
import br.com.grupokyly.apscoletor.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_TOKEN = stringPreferencesKey("jwt_token")
        val KEY_OPERATOR_NAME = stringPreferencesKey("operator_name")
        val KEY_OPERATOR_CODE = stringPreferencesKey("operator_code")
        val KEY_SUPERVISOR_NAME = stringPreferencesKey("supervisor_name")
        val KEY_SHIFT = stringPreferencesKey("shift")
        val KEY_EXPIRES_AT = longPreferencesKey("expires_at")
    }

    val sessionFlow: Flow<UserSession?> = dataStore.data.map { preferences ->
        val token = preferences[KEY_TOKEN]
        val operatorName = preferences[KEY_OPERATOR_NAME]
        val operatorCode = preferences[KEY_OPERATOR_CODE]
        val supervisorName = preferences[KEY_SUPERVISOR_NAME]
        val shift = preferences[KEY_SHIFT]
        val expiresAt = preferences[KEY_EXPIRES_AT]

        if (token != null && operatorName != null && operatorCode != null && supervisorName != null && shift != null && expiresAt != null) {
            if (Instant.now().toEpochMilli() < expiresAt) {
                UserSession(token, operatorName, operatorCode, supervisorName, shift)
            } else {
                null
            }
        } else {
            null
        }
    }

    suspend fun saveSession(response: LoginResponseDto) {
        val expiresAtMillis = try {
            Instant.parse(response.expiresAt).toEpochMilli()
        } catch (e: Exception) {
            Instant.now().plusSeconds(8 * 3600).toEpochMilli() // Fallback: 8 hours
        }

        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = response.token
            preferences[KEY_OPERATOR_NAME] = response.operatorName
            preferences[KEY_OPERATOR_CODE] = response.operatorCode
            preferences[KEY_SUPERVISOR_NAME] = response.supervisorName
            preferences[KEY_SHIFT] = response.shift
            preferences[KEY_EXPIRES_AT] = expiresAtMillis
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getToken(): String? {
        val session = sessionFlow.firstOrNull()
        return session?.token
    }

    suspend fun isSessionValid(): Boolean {
        val session = sessionFlow.firstOrNull()
        return session != null
    }
}
