package br.com.grupokyly.apscoletor.data.remote.dto

data class SyncBoxItemDto(
    val reference: String,
    val color: String,
    val size: String,
    val address: String,
    val quantityRequired: Int,
    val quantityCollected: Int,
    val status: String,
    val scannedPieces: List<ScannedPieceDto>,
    val divergences: List<DivergenceDto> = emptyList()
)
