package br.com.grupokyly.apscoletor.domain.model

data class Box(
    val id: Long = 0,
    val papeletaCode: String,
    val orderId: String,
    val status: BoxStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
    val isReopened: Boolean = false
)
