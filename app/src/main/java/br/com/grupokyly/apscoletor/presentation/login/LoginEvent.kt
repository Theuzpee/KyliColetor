package br.com.grupokyly.apscoletor.presentation.login

import android.content.Context

sealed class LoginEvent {
    data class OnSupervisorScanned(val barcode: String) : LoginEvent()
    data class OnOperatorScanned(val barcode: String) : LoginEvent()
    data class OnRegisterHardware(val context: Context) : LoginEvent()
    data class OnUnregisterHardware(val context: Context) : LoginEvent()
    object OnClearError : LoginEvent()
    data class OnLogin(val supervisorBarcode: String, val operatorBarcode: String) : LoginEvent()
}
