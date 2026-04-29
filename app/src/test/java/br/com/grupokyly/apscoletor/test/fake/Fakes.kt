package br.com.grupokyly.apscoletor.test.fake

import android.content.Context
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.usecase.FinalizeBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.OpenBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.RegisterScanUseCase
import br.com.grupokyly.apscoletor.domain.usecase.SavePartialBoxUseCase
import br.com.grupokyly.apscoletor.hardware.DataWedgeReceiver
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.domain.repository.PickingRepository
import br.com.grupokyly.apscoletor.domain.usecase.SkipPickingItemUseCase
import br.com.grupokyly.apscoletor.domain.usecase.RegisterDivergenceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeOpenBoxUseCase : OpenBoxUseCase(mockk()) {
    var result: Result<Box> = Result.success(fakeBox())
    override suspend operator fun invoke(papeletaCode: String): Result<Box> = result
}

class FakeRegisterScanUseCase : RegisterScanUseCase(mockk()) {
    var result: Result<ScanResult> = Result.success(ScanResult.Success(fakePickingItem()))
    override suspend operator fun invoke(barcode: String, boxId: Long): Result<ScanResult> = result
}

class FakeFinalizeBoxUseCase : FinalizeBoxUseCase(mockk()) {
    var result: Result<Box> = Result.success(fakeBox(status = BoxStatus.FINALIZADA))
    override suspend operator fun invoke(boxId: Long): Result<Box> = result
}

class FakeSavePartialBoxUseCase : SavePartialBoxUseCase(mockk()) {
    var result: Result<Box> = Result.success(fakeBox(status = BoxStatus.PARCIAL))
    override suspend operator fun invoke(boxId: Long): Result<Box> = result
}

class FakeDataWedgeReceiver : DataWedgeReceiver() {
    val flow = MutableSharedFlow<String>(extraBufferCapacity = 10)
    
    // We override the flow from the parent class
    override val scannedDataFlow = flow.asSharedFlow()
    
    override fun register(context: Context) {}
    override fun unregister(context: Context) {}
}


class FakePickingRepository : PickingRepository {
    var getBoxItemsResult: List<PickingItem> = emptyList()
    var skipItemResult: Result<ScanResult.ItemSkipped> = Result.success(ScanResult.ItemSkipped(fakePickingItem(), SkipReason.DESABASTECIDO))
    var registerDivergenceResult: Result<Unit> = Result.success(Unit)

    override suspend fun openBox(papeletaCode: String): Result<Box> = Result.success(fakeBox())
    override fun getBoxItems(boxId: Long): Flow<List<PickingItem>> = flowOf(getBoxItemsResult)
    override suspend fun registerScan(barcode: String, boxId: Long): Result<ScanResult> = Result.success(ScanResult.Success(fakePickingItem()))
    
    override suspend fun skipItem(pickingItemId: Long, boxId: Long, reason: SkipReason): Result<ScanResult.ItemSkipped> = skipItemResult
    
    override suspend fun registerDivergence(pickingItemId: Long, boxId: Long, barcode: String?, reason: SkipReason): Result<Unit> = registerDivergenceResult
    
    override suspend fun finalizeBox(boxId: Long): Result<Box> = Result.success(fakeBox(status = BoxStatus.FINALIZADA))
    override suspend fun savePartialBox(boxId: Long): Result<Box> = Result.success(fakeBox(status = BoxStatus.PARCIAL))
}

class FakeSkipPickingItemUseCase : SkipPickingItemUseCase(mockk()) {
    var result: Result<ScanResult.ItemSkipped> = Result.success(ScanResult.ItemSkipped(fakePickingItem(), SkipReason.DESABASTECIDO))
    override suspend operator fun invoke(pickingItemId: Long, boxId: Long, reason: SkipReason): Result<ScanResult.ItemSkipped> = result
}

class FakeRegisterDivergenceUseCase : RegisterDivergenceUseCase(mockk()) {
    var result: Result<Unit> = Result.success(Unit)
    override suspend operator fun invoke(pickingItemId: Long, boxId: Long, barcode: String?, reason: SkipReason): Result<Unit> = result
}

// Dummy mockk helper so we don't need to import mockk everywhere inside the fakes
private inline fun <reified T : Any> mockk(): T = io.mockk.mockk(relaxed = true)
