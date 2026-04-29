package br.com.grupokyly.apscoletor.domain.model

data class Divergence(
    val id: Long = 0,
    val pickingItemId: Long,
    val boxId: Long,
    val barcode: String?,
    val reason: SkipReason,
    val registeredAt: Long,
    val syncedAt: Long? = null
)
