package br.com.grupokyly.apscoletor.domain.model

sealed class AddressConfirmationState {
    object Pending : AddressConfirmationState()
    object Confirmed : AddressConfirmationState()
    object Error : AddressConfirmationState()
}
