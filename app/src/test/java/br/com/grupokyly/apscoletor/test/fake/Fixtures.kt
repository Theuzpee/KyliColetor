package br.com.grupokyly.apscoletor.test.fake

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem

fun fakeBox(status: BoxStatus = BoxStatus.EM_COLETA): Box {
    return Box(
        id = 1L,
        papeletaCode = "PAP123",
        orderId = "PED-999",
        status = status,
        createdAt = 1000L,
        updatedAt = 1000L
    )
}

fun fakePickingItem(status: ItemStatus = ItemStatus.PENDENTE): PickingItem {
    return PickingItem(
        id = 10L,
        boxId = 1L,
        reference = "REF-A",
        color = "AZUL",
        size = "M",
        address = "C37.09.6B",
        quantityRequired = 5,
        quantityCollected = if (status == ItemStatus.COMPLETO) 5 else 0,
        status = status
    )
}

fun br.com.grupokyly.apscoletor.data.remote.dto.fakeSyncBoxRequestDto(): br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto {
    return br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto(
        papeletaCode = "PAP123",
        orderId = "PED-999",
        status = "FINALIZADA",
        collectedAt = 1000L,
        items = emptyList()
    )
}

fun fakeSyncResponse(): br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxResponseDto {
    return br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxResponseDto(
        syncId = "SYNC_001", 
        syncedAt = "2025-01-01T00:00:00Z"
    )
}

fun fakeBoxEntity(syncedAt: Long? = null): br.com.grupokyly.apscoletor.data.local.entity.BoxEntity {
    return br.com.grupokyly.apscoletor.data.local.entity.BoxEntity(
        id = 1L,
        papeletaCode = "PAP123",
        orderId = "PED-999",
        status = BoxStatus.FINALIZADA,
        createdAt = 1000L,
        updatedAt = 1000L,
        syncedAt = syncedAt
    )
}

fun fakeDivergence(reason: br.com.grupokyly.apscoletor.domain.model.SkipReason = br.com.grupokyly.apscoletor.domain.model.SkipReason.DESABASTECIDO): br.com.grupokyly.apscoletor.domain.model.Divergence {
    return br.com.grupokyly.apscoletor.domain.model.Divergence(
        id = 1L,
        pickingItemId = 10L,
        boxId = 1L,
        barcode = null,
        reason = reason,
        registeredAt = 1000L
    )
}

fun fakeDivergenceEntity(reason: br.com.grupokyly.apscoletor.domain.model.SkipReason = br.com.grupokyly.apscoletor.domain.model.SkipReason.DESABASTECIDO): br.com.grupokyly.apscoletor.data.local.entity.DivergenceEntity {
    return br.com.grupokyly.apscoletor.data.local.entity.DivergenceEntity(
        id = 1L,
        pickingItemId = 10L,
        boxId = 1L,
        barcode = null,
        reason = reason,
        registeredAt = 1000L
    )
}
