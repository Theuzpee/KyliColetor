package br.com.grupokyly.apscoletor.test.fake

import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScannedPreview
import br.com.grupokyly.apscoletor.domain.model.UserSession
import br.com.grupokyly.apscoletor.data.remote.dto.LoginResponseDto
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto

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

fun fakeSyncBoxRequestDto(): SyncBoxRequestDto {
    return SyncBoxRequestDto(
        papeletaCode = "PAP123",
        orderId = "PED-999",
        status = "FINALIZADA",
        collectedAt = "1000",
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

fun fakeScannedPreview(
    barcode: String = "PECA-001",
    time: String = "14:32"
) = ScannedPreview(barcode, time)

fun fakeUserSession() = UserSession(
    token = "fake.jwt.token",
    operatorName = "João Silva",
    operatorCode = "EMP001",
    supervisorName = "Supervisor T1",
    shift = "TURNO_1"
)

fun fakeLoginResponseDto() = LoginResponseDto(
    token = "fake.jwt.token",
    operatorName = "João Silva",
    operatorCode = "EMP001",
    supervisorName = "Supervisor T1",
    shift = "TURNO_1",
    expiresAt = "2035-01-01T22:00:00Z"
)
