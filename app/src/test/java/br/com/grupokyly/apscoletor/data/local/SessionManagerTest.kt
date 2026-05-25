package br.com.grupokyly.apscoletor.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import br.com.grupokyly.apscoletor.domain.usecase.CheckSessionUseCase
import br.com.grupokyly.apscoletor.test.fake.FakeAuthRepository
import br.com.grupokyly.apscoletor.test.fake.fakeLoginResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun runSessionTest(block: suspend TestScope.(SessionManager) -> Unit) = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = this,
            produceFile = { tempFolder.newFile("test_session_${System.nanoTime()}.preferences_pb") }
        )
        val manager = SessionManager(dataStore)
        block(manager)
    }

    @Test
    fun `saveSession stores token and can be retrieved`() = runSessionTest { manager ->
        manager.saveSession(fakeLoginResponseDto())

        val token = manager.getToken()
        assertEquals("fake.jwt.token", token)
    }

    @Test
    fun `isSessionValid returns false when token is expired`() = runSessionTest { manager ->
        val expiredResponse = fakeLoginResponseDto().copy(
            expiresAt = Instant.now().minusSeconds(3600).toString()
        )
        manager.saveSession(expiredResponse)

        assertFalse(manager.isSessionValid())
    }

    @Test
    fun `isSessionValid returns false when no token saved`() = runSessionTest { manager ->
        assertFalse(manager.isSessionValid())
    }

    @Test
    fun `clearSession removes all stored data`() = runSessionTest { manager ->
        manager.saveSession(fakeLoginResponseDto())
        assertNotNull(manager.getToken())

        manager.clearSession()

        assertNull(manager.getToken())
        assertFalse(manager.isSessionValid())
    }

    @Test
    fun `CheckSessionUseCase returns false when session invalid`() = runTest {
        val fakeRepo = FakeAuthRepository().apply {
            isAuthenticatedResult = false
        }
        val useCase = CheckSessionUseCase(fakeRepo)

        assertFalse(useCase())
    }
}
