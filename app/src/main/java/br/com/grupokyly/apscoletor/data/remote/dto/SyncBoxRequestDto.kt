package br.com.grupokyly.apscoletor.data.remote.dto

data class SyncBoxRequestDto(
    val papeletaCode: String,
    val orderId: String,
    val status: String,
    val collectedAt: Long,
    val items: List<SyncBoxItemDto>
)
