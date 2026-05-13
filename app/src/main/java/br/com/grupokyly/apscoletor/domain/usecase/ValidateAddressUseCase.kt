package br.com.grupokyly.apscoletor.domain.usecase

import br.com.grupokyly.apscoletor.domain.validator.AddressValidator
import javax.inject.Inject

class ValidateAddressUseCase @Inject constructor(
    private val addressValidator: AddressValidator
) {
    operator fun invoke(scannedBarcode: String, expectedAddress: String): Boolean {
        return addressValidator.validate(scannedBarcode, expectedAddress)
    }
}
