package br.com.grupokyly.apscoletor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.grupokyly.apscoletor.domain.model.ItemStatus

@Entity(
    tableName = "picking_items",
    foreignKeys = [
        ForeignKey(
            entity = BoxEntity::class,
            parentColumns = ["id"],
            childColumns = ["boxId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("boxId")]
)
data class PickingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val boxId: Long,
    val reference: String,
    val color: String,
    val size: String,
    val address: String,
    val quantityRequired: Int,
    val quantityCollected: Int = 0,
    val status: ItemStatus
)
