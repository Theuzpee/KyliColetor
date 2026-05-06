package br.com.grupokyly.apscoletor.domain.validator

import br.com.grupokyly.apscoletor.domain.validator.AddressValidator
import javax.inject.Inject

class AddressValidatorImpl @Inject constructor() : AddressValidator {
    override fun validate(scannedBarcode: String, expectedAddress: String): Boolean {
        // Strategy 1: Exact match
        if (scannedBarcode.equals(expectedAddress, ignoreCase = true)) return true
        
        // Strategy 2: Scanned contains expected
        if (scannedBarcode.contains(expectedAddress, ignoreCase = true)) return true
        
        // Strategy 3: Expected contains scanned
        if (expectedAddress.contains(scannedBarcode, ignoreCase = true)) return true
        
        return false
    }
}
