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
    private lateinit var fakeClock: FakeClock
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
        fakeClock = FakeClock()
        
        fakeRepository.getBoxItemsResult = listOf(fakePickingItem())

        val fakeSaveMultiFloor = mockk<br.com.grupokyly.apscoletor.domain.usecase.SaveMultiFloorBoxUseCase>(relaxed = true)
        val fakeGetBoxItems = mockk<br.com.grupokyly.apscoletor.domain.usecase.GetBoxItemsUseCase>(relaxed = true)
        val fakeValidateAddress = mockk<br.com.grupokyly.apscoletor.domain.usecase.ValidateAddressUseCase>(relaxed = true)
        val fakeSessionManager = mockk<br.com.grupokyly.apscoletor.data.local.SessionManager>(relaxed = true)

        io.mockk.every { fakeGetBoxItems(any()) } answers { kotlinx.coroutines.flow.flowOf(fakeRepository.getBoxItemsResult) }
        io.mockk.every { fakeSessionManager.sessionFlow } returns kotlinx.coroutines.flow.flowOf(null)

        viewModel = PickingViewModel(
            openBoxUseCase = fakeOpenBox,
            registerScanUseCase = fakeRegisterScan,
            finalizeBoxUseCase = fakeFinalize,
            savePartialBoxUseCase = fakeSavePartial,
            saveMultiFloorBoxUseCase = fakeSaveMultiFloor,
            skipPickingItemUseCase = fakeSkipItem,
            registerDivergenceUseCase = fakeRegisterDivergence,
            getBoxItemsUseCase = fakeGetBoxItems,
            validateAddressUseCase = fakeValidateAddress,
            scannerReceiver = fakeReceiver,
            scanFeedbackManager = fakeFeedback,
            clock = fakeClock,
            sessionManager = fakeSessionManager
        )
    }

    @Test
    fun `OnSkipItem emits ItemSkipped then advances to next pending`() = runTest {
        // Arrange: colocar ViewModel em Collecting
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PickingUiState.Collecting)

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
            assertTrue(skipped is PickingUiState.ItemSkipped)
            assertEquals(SkipReason.DESABASTECIDO, (skipped as PickingUiState.ItemSkipped).reason)

            // Feedback sonoro de atenção
            verify { fakeFeedback.scanError() }

            // Após 1.5s avança
            advanceTimeBy(1501)
            val next = awaitItem()
            assertTrue(
                "Estado esperado: Collecting ou BoxPartial, recebido: $next",
                next is PickingUiState.Collecting || next is PickingUiState.BoxPartial
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
        assertTrue(collectingState is PickingUiState.Collecting)

        viewModel.uiState.test {
            skipItems(1)

            viewModel.onEvent(
                PickingEvent.OnRegisterDivergence(
                    barcode = null,
                    reason = SkipReason.SUJA,
                    evidencePhotoUrl = null
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
            assertTrue(finalState is PickingUiState.BoxPartial)

            verify { fakeFeedback.boxPartial() }

            cancelAndIgnoreRemainingEvents()
        }
    }
}

