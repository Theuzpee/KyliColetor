package br.com.grupokyly.apscoletor.domain.model

sealed class ScanResult {
    data class Success(val item: PickingItem) : ScanResult()
    object SkuNotFound : ScanResult()
    object AlreadyScanned : ScanResult()
    data class QuantityComplete(val lastPieceBarcode: String) : ScanResult()
    data class ItemSkipped(val item: PickingItem, val reason: SkipReason) : ScanResult()
}
