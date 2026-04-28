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

// Dummy mockk helper so we don't need to import mockk everywhere inside the fakes
private inline fun <reified T : Any> mockk(): T = io.mockk.mockk(relaxed = true)
