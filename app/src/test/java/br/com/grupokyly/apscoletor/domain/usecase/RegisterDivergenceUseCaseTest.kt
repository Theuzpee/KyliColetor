package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import br.com.grupokyly.apscoletor.test.fake.FakePickingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RegisterDivergenceUseCaseTest {

    @Test
    fun `invoke with invalid ids returns failure`() = runTest {
        val useCase = RegisterDivergenceUseCase(FakePickingRepository())

        val result = useCase(
            pickingItemId = 0L,
            boxId = 0L,
            barcode = null,
            reason = SkipReason.SUJA
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke success does not change PickingItem status`() = runTest {
        val mockRepo = mockk<PickingRepository>()
        var itemStatusChanged = false

        coEvery {
            mockRepo.registerDivergence(any(), any(), any(), any())
        } returns Result.success(Unit)

        // No PickingRepository não existe método independente de updateItemStatus.
        // Se houvesse algum efeito colateral como registerScan(), nós validaríamos.
        // O repositório real sabe que não altera status, então verificamos se registerScan não foi chamado.
        coEvery {
            mockRepo.registerScan(any(), any(), any())
        } answers {
            itemStatusChanged = true
            Result.success(mockk())
        }
        
        coEvery {
            mockRepo.skipItem(any(), any(), any())
        } answers {
            itemStatusChanged = true
            Result.success(mockk())
        }

        val useCase = RegisterDivergenceUseCase(mockRepo)
        val result = useCase(1L, 1L, null, SkipReason.SUJA)

        assertTrue(result.isSuccess)
        assertFalse(itemStatusChanged)
        coVerify(exactly = 0) { mockRepo.registerScan(any(), any(), any()) }
        coVerify(exactly = 0) { mockRepo.skipItem(any(), any(), any()) }
    }

    @Test
    fun `invoke with null barcode registers divergence successfully`() = runTest {
        val fakeRepo = FakePickingRepository()
        val useCase = RegisterDivergenceUseCase(fakeRepo)

        val result = useCase(
            pickingItemId = 1L,
            boxId = 1L,
            barcode = null,  // não leu o código
            reason = SkipReason.NAO_LE_CODIGO
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke accepts all defect skip reasons`() = runTest {
        val defectReasons = listOf(
            SkipReason.SUJA,
            SkipReason.AMASSADA,
            SkipReason.DESEMBALADA,
            SkipReason.DESCASCADA,
            SkipReason.TAG_ERRADO,
            SkipReason.NAO_LE_CODIGO
        )

        defectReasons.forEach { reason ->
            val useCase = RegisterDivergenceUseCase(FakePickingRepository())
            val result = useCase(1L, 1L, null, reason)
            assertTrue(result.isSuccess, "Falhou para reason: $reason")
        }
    }
}
