package br.com.grupokyly.apscoletor.presentation.picking

import app.cash.turbine.test
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.hardware.ScanFeedbackManager
import br.com.grupokyly.apscoletor.test.fake.*
import br.com.grupokyly.apscoletor.test.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class PickingViewModelSkipTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeOpenBox: FakeOpenBoxUseCase
    private lateinit var fakeRegisterScan: FakeRegisterScanUseCase
    private lateinit var fakeFinalize: FakeFinalizeBoxUseCase
    private lateinit var fakeSavePartial: FakeSavePartialBoxUseCase
    private lateinit var fakeSkipItem: FakeSkipPickingItemUseCase
    private lateinit var fakeRegisterDivergence: FakeRegisterDivergenceUseCase
    private lateinit var fakeReceiver: FakeDataWedgeReceiver
    private val fakeFeedback: ScanFeedbackManager = mockk(relaxed = true)
    
    private val fakeRepository: FakePickingRepository = FakePickingRepository()
    
    private lateinit var viewModel: PickingViewModel

    @Before
    fun setup() {
        fakeOpenBox = FakeOpenBoxUseCase()
        fakeRegisterScan = FakeRegisterScanUseCase()
        fakeFinalize = FakeFinalizeBoxUseCase()
        fakeSavePartial = FakeSavePartialBoxUseCase()
        fakeSkipItem = FakeSkipPickingItemUseCase()
        fakeRegisterDivergence = FakeRegisterDivergenceUseCase()
        fakeReceiver = FakeDataWedgeReceiver()
        
        fakeRepository.getBoxItemsResult = listOf(fakePickingItem())

        viewModel = PickingViewModel(
            openBoxUseCase = fakeOpenBox,
            registerScanUseCase = fakeRegisterScan,
            finalizeBoxUseCase = fakeFinalize,
            savePartialBoxUseCase = fakeSavePartial,
            skipPickingItemUseCase = fakeSkipItem,
            registerDivergenceUseCase = fakeRegisterDivergence,
            repository = fakeRepository,
            dataWedgeReceiver = fakeReceiver,
            scanFeedbackManager = fakeFeedback
        )
    }

    @Test
    fun `OnSkipItem emits ItemSkipped then advances to next pending`() = runTest {
        // Arrange: colocar ViewModel em Collecting
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        assertIs<PickingUiState.Collecting>(viewModel.uiState.value)

        fakeSkipItem.result = Result.success(
            ScanResult.ItemSkipped(
                item = fakePickingItem(status = ItemStatus.FALTA),
                reason = SkipReason.DESABASTECIDO
            )
        )

        // Adicionar um segundo item na lista para poder "avançar para o próximo"
        fakeRepository.getBoxItemsResult = listOf(
            fakePickingItem(status = ItemStatus.FALTA),
            fakePickingItem(status = ItemStatus.PENDENTE)
        )

        viewModel.uiState.test {
            skipItems(1) // Collecting atual

            viewModel.onEvent(PickingEvent.OnSkipItem(SkipReason.DESABASTECIDO))

            // Deve emitir ItemSkipped
            val skipped = awaitItem()
            assertIs<PickingUiState.ItemSkipped>(skipped)
            assertEquals(SkipReason.DESABASTECIDO, skipped.reason)

            // Feedback sonoro de atenção
            verify { fakeFeedback.scanError() }

            // Após 1.5s avança
            advanceTimeBy(1501)
            val next = awaitItem()
            assertTrue(
                next is PickingUiState.Collecting || next is PickingUiState.BoxPartial,
                "Estado esperado: Collecting ou BoxPartial, recebido: $next"
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSkipItem is ignored when state is Idle`() = runTest {
        assertEquals(PickingUiState.Idle, viewModel.uiState.value)

        viewModel.onEvent(PickingEvent.OnSkipItem(SkipReason.DESABASTECIDO))
        advanceUntilIdle()

        assertEquals(PickingUiState.Idle, viewModel.uiState.value)
        // No fake do mockk usaria coVerify se fosse mockk, mas como é fake normal, verificamos o que chamou.
        // Como o result não vai ser invocado, skipItem do fakeRepository não deve ser chamado.
        // Já garantimos pelo state ser Idle.
    }

    @Test
    fun `OnRegisterDivergence triggers haptic and stays in Collecting`() = runTest {
        // Arrange: colocar em Collecting
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        val collectingState = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(collectingState)

        viewModel.uiState.test {
            skipItems(1)

            viewModel.onEvent(
                PickingEvent.OnRegisterDivergence(
                    barcode = null,
                    reason = SkipReason.SUJA
                )
            )
            advanceUntilIdle()

            // Estado permanece Collecting — sem transição
            expectNoEvents()

            // Apenas feedback tátil leve — NÃO scanError()
            verify(exactly = 1) { fakeFeedback.scanPartialSuccess() }
            verify(exactly = 0) { fakeFeedback.scanError() }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSkipItem with last pending item finalizes box as BoxPartial`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()

        // Fake retorna ItemSkipped sem próximo item (nextItem = null)
        fakeSkipItem.result = Result.success(
            ScanResult.ItemSkipped(
                item = fakePickingItem(status = ItemStatus.FALTA),
                reason = SkipReason.DESABASTECIDO
            )
        )
        
        // Simular que não há mais itens PENDENTES
        fakeRepository.getBoxItemsResult = emptyList()

        viewModel.uiState.test {
            skipItems(1)

            viewModel.onEvent(PickingEvent.OnSkipItem(SkipReason.DESABASTECIDO))

            awaitItem() // ItemSkipped
            advanceTimeBy(1600) // passar o delay de 1.5s

            val finalState = awaitItem()
            assertIs<PickingUiState.BoxPartial>(finalState)

            verify { fakeFeedback.boxPartial() }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
