package br.com.grupokyly.apscoletor.presentation.picking

import android.content.Context

sealed class PickingEvent {
    data class OnPapeletaScanned(val code: String) : PickingEvent()
    data class OnPieceScan(val barcode: String) : PickingEvent()
    object OnFinalizeBox : PickingEvent()
    object OnSavePartial : PickingEvent()
    data class OnRegisterHardware(val context: Context) : PickingEvent()
    data class OnUnregisterHardware(val context: Context) : PickingEvent()
}
