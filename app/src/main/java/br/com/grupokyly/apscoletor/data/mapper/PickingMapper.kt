package br.com.grupokyly.apscoletor.data.mapper

import br.com.grupokyly.apscoletor.data.local.entity.BoxEntity
import br.com.grupokyly.apscoletor.data.local.entity.PickingItemEntity
import br.com.grupokyly.apscoletor.data.local.entity.ScannedPieceEntity
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScannedPiece
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxRequestDto
import br.com.grupokyly.apscoletor.data.remote.dto.SyncBoxItemDto
import br.com.grupokyly.apscoletor.data.remote.dto.ScannedPieceDto
import br.com.grupokyly.apscoletor.data.remote.dto.DivergenceDto
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.domain.model.Divergence
import br.com.grupokyly.apscoletor.data.local.entity.DivergenceEntity

fun DivergenceEntity.toDomain(): Divergence {
    return Divergence(
        id = this.id,
        pickingItemId = this.pickingItemId,
        boxId = this.boxId,
        barcode = this.barcode,
        reason = this.reason,
        registeredAt = this.registeredAt,
        syncedAt = this.syncedAt
    )
}

fun BoxEntity.toDomain(): Box {
    return Box(
        id = id,
        papeletaCode = papeletaCode,
        orderId = orderId,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedAt = syncedAt
    )
}

fun PickingItemEntity.toDomain(): PickingItem {
    return PickingItem(
        id = id,
        boxId = boxId,
        reference = reference,
        color = color,
        size = size,
        address = address,
        quantityRequired = quantityRequired,
        quantityCollected = quantityCollected,
        status = status
    )
}

fun ScannedPieceEntity.toDomain(): ScannedPiece {
    return ScannedPiece(
        id = id,
        pickingItemId = pickingItemId,
        barcode = barcode,
        scannedAt = scannedAt
    )
}

fun Box.toSyncRequestDto(
    items: List<PickingItem>,
    piecesByItem: Map<Long, List<ScannedPiece>>,
    divergencesByItem: Map<Long, List<Divergence>> = emptyMap()
): SyncBoxRequestDto {
    return SyncBoxRequestDto(
        papeletaCode = papeletaCode,
        orderId = orderId,
        status = status.name,
        collectedAt = updatedAt,
        items = items.map { item ->
            SyncBoxItemDto(
                reference = item.reference,
                color = item.color,
                size = item.size,
                address = item.address,
                quantityRequired = item.quantityRequired,
                quantityCollected = item.quantityCollected,
                status = item.status.name,
                scannedPieces = piecesByItem[item.id]?.map { piece ->
                    ScannedPieceDto(
                        barcode = piece.barcode,
                        scannedAt = piece.scannedAt
                    )
                } ?: emptyList(),
                divergences = divergencesByItem[item.id]?.map { div ->
                    DivergenceDto(
                        reason = div.reason.name,
                        barcode = div.barcode,
                        registeredAt = div.registeredAt
                    )
                } ?: emptyList()
            )
        }
    )
}
