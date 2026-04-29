package br.com.grupokyly.apscoletor.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncBoxRequestDto(
    val papeletaCode: String,
    val orderId: String,
    val status: String,
    val collectedAt: Long,
    val items: List<SyncBoxItemDto>
)

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

data class DivergenceDto(
    @SerializedName("reason") val reason: String,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("registeredAt") val registeredAt: Long
)

data class ScannedPieceDto(
    val barcode: String,
    val scannedAt: Long
)
