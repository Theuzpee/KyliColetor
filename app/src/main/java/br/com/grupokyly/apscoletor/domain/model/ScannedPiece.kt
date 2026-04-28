package br.com.grupokyly.apscoletor.domain.model

data class ScannedPiece(
    val id: Long = 0,
    val pickingItemId: Long,
    val barcode: String,
    val scannedAt: Long
)
