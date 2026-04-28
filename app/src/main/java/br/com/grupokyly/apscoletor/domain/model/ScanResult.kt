package br.com.grupokyly.apscoletor.domain.model

sealed class ScanResult {
    data class Success(val item: PickingItem) : ScanResult()
    object SkuNotFound : ScanResult()
    object AlreadyScanned : ScanResult()
    data class QuantityComplete(val item: PickingItem) : ScanResult()
}
