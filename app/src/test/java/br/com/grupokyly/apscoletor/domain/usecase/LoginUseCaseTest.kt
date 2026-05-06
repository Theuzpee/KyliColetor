package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.test.fake.FakeAuthRepository
import br.com.grupokyly.apscoletor.test.fake.fakeUserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginUseCaseTest {

    @Test
    fun `invoke with blank supervisorBarcode returns failure`() = runTest {
        val useCase = LoginUseCase(FakeAuthRepository())

        val result = useCase("", "OP-001")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("supervisor", ignoreCase = true))
    }

    @Test
    fun `invoke with blank operatorBarcode returns failure`() = runTest {
        val useCase = LoginUseCase(FakeAuthRepository())

        val result = useCase("SUP-001", "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("colaborador", ignoreCase = true))
    }

    @Test
    fun `invoke with same barcodes returns failure`() = runTest {
        val useCase = LoginUseCase(FakeAuthRepository())

        val result = useCase("MESMO-CODE", "MESMO-CODE")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("mesmo", ignoreCase = true))
    }

    @Test
    fun `invoke with valid barcodes returns UserSession`() = runTest {
        val expected = fakeUserSession()
        val fakeRepo = FakeAuthRepository().apply {
            loginResult = Result.success(expected)
        }
        val useCase = LoginUseCase(fakeRepo)

        val result = useCase("SUP-001", "OP-001")

        assertTrue(result.isSuccess)
        assertEquals(expected.operatorCode, result.getOrNull()!!.operatorCode)
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val fakeRepo = FakeAuthRepository().apply {
            loginResult = Result.failure(Exception("Crachá não reconhecido."))
        }
        val useCase = LoginUseCase(fakeRepo)

        val result = useCase("SUP-001", "OP-001")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("reconhecido", ignoreCase = true))
    }
}
