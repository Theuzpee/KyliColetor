package br.com.grupokyly.apscoletor.presentation.picking

import android.content.Context
import br.com.grupokyly.apscoletor.domain.model.SkipReason

sealed class PickingEvent {
    data class OnPapeletaScanned(val code: String) : PickingEvent()
    data class OnAddressScan(val barcode: String) : PickingEvent()
    data class OnPieceScan(val barcode: String) : PickingEvent()
    object OnFinalizeBox : PickingEvent()
    object OnSavePartial : PickingEvent()
    object OnSaveMultiFloor : PickingEvent()
    object OnAdvanceToNextItem : PickingEvent()
    object OnResumeBoxConfirmed : PickingEvent()
    data class OnRegisterHardware(val context: Context) : PickingEvent()
    data class OnUnregisterHardware(val context: Context) : PickingEvent()
    data class OnSkipItem(val reason: SkipReason) : PickingEvent()
    data class OnRegisterDivergence(val barcode: String?, val reason: SkipReason, val evidencePhotoUrl: String?) : PickingEvent()
    data class OnDebugScan(val barcode: String) : PickingEvent()
    data class OnManualInput(val code: String) : PickingEvent()
}
