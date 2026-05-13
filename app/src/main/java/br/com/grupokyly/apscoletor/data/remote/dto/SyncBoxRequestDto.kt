package br.com.grupokyly.apscoletor.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncBoxRequestDto(
    val papeletaCode: String,
    val orderId: String,
    val status: String,
    val collectedAt: String,
    val items: List<SyncBoxItemDto>
)

data class DivergenceDto(
    @SerializedName("reason") val reason: String,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("registeredAt") val registeredAt: String,
    @SerializedName("evidencePhotoUrl") val evidencePhotoUrl: String? = null
)
