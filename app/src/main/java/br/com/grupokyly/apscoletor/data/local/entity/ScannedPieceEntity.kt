package br.com.grupokyly.apscoletor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_pieces",
    foreignKeys = [
        ForeignKey(
            entity = PickingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["pickingItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pickingItemId"), Index("barcode", unique = true)] // unique per barcode in the whole DB might be needed or per piece. We'll leave unique if barcode is absolutely unique globally. The prompt says "barcode: String (código único da peça bipada)".
)
data class ScannedPieceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pickingItemId: Long,
    val barcode: String,
    val scannedAt: Long
)
