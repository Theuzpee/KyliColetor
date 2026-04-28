package br.com.grupokyly.apscoletor.domain.model

data class PickingItem(
    val id: Long = 0,
    val boxId: Long,
    val reference: String,
    val color: String,
    val size: String,
    val address: String,
    val quantityRequired: Int,
    val quantityCollected: Int,
    val status: ItemStatus
)
