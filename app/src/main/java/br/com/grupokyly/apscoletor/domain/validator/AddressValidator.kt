package br.com.grupokyly.apscoletor.domain.validator

interface AddressValidator {
    fun validate(scannedBarcode: String, expectedAddress: String): Boolean
}
