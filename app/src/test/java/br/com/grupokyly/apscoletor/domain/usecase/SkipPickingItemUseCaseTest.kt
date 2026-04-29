package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.test.fake.FakePickingRepository
import br.com.grupokyly.apscoletor.test.fake.fakePickingItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertIs

class SkipPickingItemUseCaseTest {

    @Test
    fun `invoke with invalid pickingItemId returns failure`() = runTest {
        val useCase = SkipPickingItemUseCase(FakePickingRepository())

        val result = useCase(
            pickingItemId = 0L,  // inválido
            boxId = 1L,
            reason = SkipReason.DESABASTECIDO
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!
            .contains("inválido", ignoreCase = true))
    }

    @Test
    fun `invoke with invalid boxId returns failure`() = runTest {
        val useCase = SkipPickingItemUseCase(FakePickingRepository())

        val result = useCase(
            pickingItemId = 1L,
            boxId = -1L,  // inválido
            reason = SkipReason.DESABASTECIDO
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with valid params returns ItemSkipped`() = runTest {
        val expectedItem = fakePickingItem(status = ItemStatus.FALTA)
        val fakeRepo = FakePickingRepository().apply {
            skipItemResult = Result.success(
                ScanResult.ItemSkipped(expectedItem, SkipReason.DESABASTECIDO)
            )
        }
        val useCase = SkipPickingItemUseCase(fakeRepo)

        val result = useCase(1L, 1L, SkipReason.DESABASTECIDO)

        assertTrue(result.isSuccess)
        val scanResult = result.getOrNull()!!
        assertIs<ScanResult.ItemSkipped>(scanResult)
        assertEquals(SkipReason.DESABASTECIDO, scanResult.reason)
        assertEquals(ItemStatus.FALTA, scanResult.item.status)
    }
}
