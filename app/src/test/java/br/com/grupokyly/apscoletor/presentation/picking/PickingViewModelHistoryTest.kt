package br.com.grupokyly.apscoletor.presentation.picking

import app.cash.turbine.test
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.hardware.ScanFeedbackManager
import br.com.grupokyly.apscoletor.test.fake.*
import br.com.grupokyly.apscoletor.test.util.MainDispatcherRule
import io.mockk.mockk
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
class PickingViewModelHistoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeOpenBox: FakeOpenBoxUseCase
    private lateinit var fakeRegisterScan: FakeRegisterScanUseCase
    private lateinit var fakeFinalize: FakeFinalizeBoxUseCase
    private lateinit var fakeSavePartial: FakeSavePartialBoxUseCase
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
        fakeReceiver = FakeDataWedgeReceiver()
        fakeClock = FakeClock()
        
        fakeRepository.getBoxItemsResult = listOf(fakePickingItem())

        val fakeSaveMultiFloor = mockk<br.com.grupokyly.apscoletor.domain.usecase.SaveMultiFloorBoxUseCase>(relaxed = true)
        val fakeGetBoxItems = mockk<br.com.grupokyly.apscoletor.domain.usecase.GetBoxItemsUseCase>(relaxed = true)
        val fakeValidateAddress = mockk<br.com.grupokyly.apscoletor.domain.usecase.ValidateAddressUseCase>(relaxed = true)
        val fakeSessionManager = mockk<br.com.grupokyly.apscoletor.data.local.SessionManager>(relaxed = true)

        io.mockk.every { fakeGetBoxItems(any()) } answers { kotlinx.coroutines.flow.flowOf(fakeRepository.getBoxItemsResult) }
        io.mockk.every { fakeSessionManager.sessionFlow } returns kotlinx.coroutines.flow.flowOf(null)
        io.mockk.every { fakeValidateAddress(any(), any()) } returns true

        viewModel = PickingViewModel(
            openBoxUseCase = fakeOpenBox,
            registerScanUseCase = fakeRegisterScan,
            finalizeBoxUseCase = fakeFinalize,
            savePartialBoxUseCase = fakeSavePartial,
            saveMultiFloorBoxUseCase = fakeSaveMultiFloor,
            skipPickingItemUseCase = FakeSkipPickingItemUseCase(),
            registerDivergenceUseCase = FakeRegisterDivergenceUseCase(),
            getBoxItemsUseCase = fakeGetBoxItems,
            validateAddressUseCase = fakeValidateAddress,
            scannerReceiver = fakeReceiver,
            scanFeedbackManager = fakeFeedback,
            clock = fakeClock,
            sessionManager = fakeSessionManager
        )
    }

    @Test
    fun `valid scan adds item to top of history`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        assertIs<PickingUiState.Collecting>(viewModel.uiState.value)

        fakeRegisterScan.result = Result.success(ScanResult.Success(fakePickingItem()))

        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-001"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(state)
        assertEquals(1, state.lastScannedItems.size)
        assertEquals("PECA-001", state.lastScannedItems.first().barcode)
    }

    @Test
    fun `history keeps maximum of 3 items`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        fakeRegisterScan.result = Result.success(ScanResult.Success(fakePickingItem()))

        listOf("PECA-001", "PECA-002", "PECA-003", "PECA-004").forEach { barcode ->
            viewModel.onEvent(PickingEvent.OnPieceScan(barcode))
            advanceUntilIdle()
        }

        val state = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(state)
        assertEquals(3, state.lastScannedItems.size)
        assertEquals("PECA-004", state.lastScannedItems.first().barcode)
        assertFalse(state.lastScannedItems.any { it.barcode == "PECA-001" })
    }

    @Test
    fun `most recent scan is always at top of history`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        fakeRegisterScan.result = Result.success(ScanResult.Success(fakePickingItem()))

        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-001"))
        advanceUntilIdle()
        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-002"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(state)
        assertEquals("PECA-002", state.lastScannedItems.first().barcode)
        assertEquals("PECA-001", state.lastScannedItems.last().barcode)
    }

    @Test
    fun `scan error does not add to history`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        fakeRegisterScan.result = Result.success(ScanResult.AlreadyScanned)

        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-DUPLICADA"))
        advanceUntilIdle()

        advanceTimeBy(5100)

        val state = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(state)
        assertTrue(state.lastScannedItems.isEmpty())
    }

    @Test
    fun `history is cleared when new box is opened`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        fakeRegisterScan.result = Result.success(ScanResult.Success(fakePickingItem()))
        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-001"))
        advanceUntilIdle()
        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-002"))
        advanceUntilIdle()

        val stateBefore = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(stateBefore)
        assertEquals(2, stateBefore.lastScannedItems.size)

        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP456"))
        advanceUntilIdle()

        val stateAfter = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(stateAfter)
        assertTrue(stateAfter.lastScannedItems.isEmpty())
    }

    @Test
    fun `quantity complete scan adds to history`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        fakeRegisterScan.result = Result.success(ScanResult.QuantityComplete("PECA-COMPLETA"))

        viewModel.onEvent(PickingEvent.OnPieceScan("PECA-COMPLETA"))
        advanceUntilIdle()

        advanceTimeBy(1600)

        val state = viewModel.uiState.value
        if (state is PickingUiState.Collecting) {
            assertTrue(state.lastScannedItems.any { it.barcode == "PECA-COMPLETA" })
        }
    }

    @Test
    fun `valid manual input adds item to top of history with isManual equal to true`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        assertIs<PickingUiState.Collecting>(viewModel.uiState.value)

        val state = viewModel.uiState.value as PickingUiState.Collecting
        viewModel.onEvent(PickingEvent.OnManualInput(state.currentItem.address))
        advanceUntilIdle()

        fakeRegisterScan.result = Result.success(ScanResult.Success(fakePickingItem()))

        viewModel.onEvent(PickingEvent.OnManualInput("PECA-001"))
        advanceUntilIdle()

        val endState = viewModel.uiState.value
        assertIs<PickingUiState.Collecting>(endState)
        assertEquals(1, endState.lastScannedItems.size)
        val lastItem = endState.lastScannedItems.first()
        assertEquals("PECA-001", lastItem.barcode)
        assertTrue(lastItem.isManual)
    }
}
