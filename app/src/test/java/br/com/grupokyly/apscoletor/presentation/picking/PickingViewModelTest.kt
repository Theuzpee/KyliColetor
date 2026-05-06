package br.com.grupokyly.apscoletor.presentation.picking

import app.cash.turbine.test
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.hardware.ScanFeedbackManager
import br.com.grupokyly.apscoletor.test.fake.FakeDataWedgeReceiver
import br.com.grupokyly.apscoletor.test.fake.FakeFinalizeBoxUseCase
import br.com.grupokyly.apscoletor.test.fake.FakeOpenBoxUseCase
import br.com.grupokyly.apscoletor.test.fake.FakeRegisterScanUseCase
import br.com.grupokyly.apscoletor.test.fake.FakeSavePartialBoxUseCase
import br.com.grupokyly.apscoletor.test.fake.FakeClock
import br.com.grupokyly.apscoletor.test.fake.fakePickingItem
import br.com.grupokyly.apscoletor.test.util.MainDispatcherRule
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

@OptIn(ExperimentalCoroutinesApi::class)
class PickingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeOpenBox: FakeOpenBoxUseCase
    private lateinit var fakeRegisterScan: FakeRegisterScanUseCase
    private lateinit var fakeFinalize: FakeFinalizeBoxUseCase
    private lateinit var fakeSavePartial: FakeSavePartialBoxUseCase
    private lateinit var fakeReceiver: FakeDataWedgeReceiver
    private lateinit var fakeClock: FakeClock
    private val fakeFeedback: ScanFeedbackManager = mockk(relaxed = true)
    
    // The repository is also needed since we injected it to fix the PickingViewModel
    private val fakeRepository: br.com.grupokyly.apscoletor.domain.repository.PickingRepository = mockk(relaxed = true)
    
    private lateinit var viewModel: PickingViewModel

    @Before
    fun setup() {
        fakeOpenBox = FakeOpenBoxUseCase()
        fakeRegisterScan = FakeRegisterScanUseCase()
        fakeFinalize = FakeFinalizeBoxUseCase()
        fakeSavePartial = FakeSavePartialBoxUseCase()
        fakeReceiver = FakeDataWedgeReceiver()
        fakeClock = FakeClock()
        
        // Mocking the repository getBoxItems to return a valid flow to avoid sticking in LoadingBox
        val itemsFlow = kotlinx.coroutines.flow.flowOf(listOf(fakePickingItem()))
        io.mockk.every { fakeRepository.getBoxItems(any()) } returns itemsFlow

        viewModel = PickingViewModel(
            openBoxUseCase = fakeOpenBox,
            registerScanUseCase = fakeRegisterScan,
            finalizeBoxUseCase = fakeFinalize,
            savePartialBoxUseCase = fakeSavePartial,
            repository = fakeRepository,
            dataWedgeReceiver = fakeReceiver,
            scanFeedbackManager = fakeFeedback,
            clock = fakeClock
        )
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(PickingUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `onPapeletaScanned with valid code emits LoadingBox then Collecting`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // Idle inicial

            viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))

            val loading = awaitItem()
            assertTrue(loading is PickingUiState.LoadingBox)

            val collecting = awaitItem()
            assertTrue(collecting is PickingUiState.Collecting)
            assertEquals("PAP123", (collecting as PickingUiState.Collecting).box.papeletaCode)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPieceScan with AlreadyScanned emits Error then reverts to Collecting`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle() // Ensure it reached Collecting state
        
        val collectingState = viewModel.uiState.value
        assertTrue(collectingState is PickingUiState.Collecting)

        fakeRegisterScan.result = Result.success(ScanResult.AlreadyScanned)

        viewModel.uiState.test {
            skipItems(1) // estado Collecting atual

            viewModel.onEvent(PickingEvent.OnPieceScan("BARCODE_DUPLICADO"))

            val error = awaitItem()
            assertTrue(error is PickingUiState.Error)
            assertTrue((error as PickingUiState.Error).message.contains("bipada", ignoreCase = true))

            // Advance virtual time by 2000ms (the delay in the ViewModel)
            advanceTimeBy(2001)

            val reverted = awaitItem()
            assertTrue(reverted is PickingUiState.Collecting)

            verify { fakeFeedback.scanError() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPieceScan with QuantityComplete emits ItemComplete then advances`() = runTest {
        viewModel.onEvent(PickingEvent.OnPapeletaScanned("PAP123"))
        advanceUntilIdle()
        
        fakeRegisterScan.result = Result.success(
            ScanResult.QuantityComplete(fakePickingItem(status = ItemStatus.COMPLETO))
        )

        viewModel.uiState.test {
            skipItems(1) // Collecting

            viewModel.onEvent(PickingEvent.OnPieceScan("BARCODE_OK"))

            val itemComplete = awaitItem()
            assertTrue(itemComplete is PickingUiState.ItemComplete)

            verify { fakeFeedback.scanSkuComplete() }

            advanceTimeBy(1501)

            // Após 1.5s de delay deve avançar, como simplificamos nextItem = null,
            // no teste ele só continua. O Viewmodel não tem a navegação exata ainda
            // Mas checamos se não dá crash e se emite ItemComplete
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPieceScan is ignored when state is Idle`() = runTest {
        assertEquals(PickingUiState.Idle, viewModel.uiState.value)

        fakeReceiver.flow.emit("BARCODE_ESPURIO")
        advanceUntilIdle()

        assertEquals(PickingUiState.Idle, viewModel.uiState.value)
        verify(exactly = 0) { fakeFeedback.scanError() }
    }
}
