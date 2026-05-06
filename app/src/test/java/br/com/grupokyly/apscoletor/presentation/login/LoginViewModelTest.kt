package br.com.grupokyly.apscoletor.presentation.login

import app.cash.turbine.test
import br.com.grupokyly.apscoletor.domain.usecase.LoginUseCase
import br.com.grupokyly.apscoletor.hardware.ScanFeedbackManager
import br.com.grupokyly.apscoletor.test.fake.FakeAuthRepository
import br.com.grupokyly.apscoletor.test.fake.FakeDataWedgeReceiver
import br.com.grupokyly.apscoletor.test.fake.fakeUserSession
import br.com.grupokyly.apscoletor.test.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var fakeReceiver: FakeDataWedgeReceiver
    private val fakeFeedback: ScanFeedbackManager = mockk(relaxed = true)
    
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        fakeAuthRepo = FakeAuthRepository()
        fakeReceiver = FakeDataWedgeReceiver()

        viewModel = LoginViewModel(
            loginUseCase = LoginUseCase(fakeAuthRepo),
            dataWedgeReceiver = fakeReceiver,
            scanFeedbackManager = fakeFeedback
        )
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `OnSupervisorScanned emits WaitingOperator`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // Idle

            viewModel.onEvent(LoginEvent.OnSupervisorScanned("SUP-001"))

            val state = awaitItem()
            assertIs<LoginUiState.WaitingOperator>(state)
            verify { fakeFeedback.scanPartialSuccess() }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login success emits Success state`() = runTest {
        fakeAuthRepo.loginResult = Result.success(fakeUserSession())

        viewModel.uiState.test {
            skipItems(1)

            viewModel.onEvent(LoginEvent.OnSupervisorScanned("SUP-001"))
            skipItems(1) // WaitingOperator

            viewModel.onEvent(LoginEvent.OnOperatorScanned("OP-001"))

            val loading = awaitItem()
            assertIs<LoginUiState.Loading>(loading)

            val success = awaitItem()
            assertIs<LoginUiState.Success>(success)
            assertEquals("EMP001", success.session.operatorCode)
            verify { fakeFeedback.scanSkuComplete() }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login error emits Error then reverts to Idle after 3s`() = runTest {
        fakeAuthRepo.loginResult = Result.failure(Exception("Crachá não reconhecido."))

        viewModel.onEvent(LoginEvent.OnSupervisorScanned("SUP-001"))
        advanceUntilIdle()

        viewModel.uiState.test {
            skipItems(1) // WaitingOperator

            viewModel.onEvent(LoginEvent.OnOperatorScanned("OP-INVALIDO"))

            awaitItem() // Loading
            val error = awaitItem()
            assertIs<LoginUiState.Error>(error)
            assertTrue(error.message.contains("reconhecido", ignoreCase = true))
            verify { fakeFeedback.scanError() }

            advanceTimeBy(3100)
            val idle = awaitItem()
            assertIs<LoginUiState.Idle>(idle)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnOperatorScanned is ignored when state is Idle`() = runTest {
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)

        viewModel.onEvent(LoginEvent.OnOperatorScanned("OP-001"))
        advanceUntilIdle()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        // Login should not have been called
    }
}
