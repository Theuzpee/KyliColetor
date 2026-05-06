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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testScope = TestScope()
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFolder.newFile("test_session.preferences_pb") }
        )
        sessionManager = SessionManager(dataStore)
    }

    @Test
    fun `saveSession stores token and can be retrieved`() = testScope.runTest {
        sessionManager.saveSession(fakeLoginResponseDto())

        val token = sessionManager.getToken()
        assertEquals("fake.jwt.token", token)
    }

    @Test
    fun `isSessionValid returns false when token is expired`() = testScope.runTest {
        val expiredResponse = fakeLoginResponseDto().copy(
            expiresAt = Instant.now().minusSeconds(3600).toString()
        )
        sessionManager.saveSession(expiredResponse)

        assertFalse(sessionManager.isSessionValid())
    }

    @Test
    fun `isSessionValid returns false when no token saved`() = testScope.runTest {
        assertFalse(sessionManager.isSessionValid())
    }

    @Test
    fun `clearSession removes all stored data`() = testScope.runTest {
        sessionManager.saveSession(fakeLoginResponseDto())
        assertNotNull(sessionManager.getToken())

        sessionManager.clearSession()

        assertNull(sessionManager.getToken())
        assertFalse(sessionManager.isSessionValid())
    }

    @Test
    fun `CheckSessionUseCase returns false when session invalid`() = testScope.runTest {
        val fakeRepo = FakeAuthRepository().apply {
            isAuthenticatedResult = false
        }
        val useCase = CheckSessionUseCase(fakeRepo)

        assertFalse(useCase())
    }
}
