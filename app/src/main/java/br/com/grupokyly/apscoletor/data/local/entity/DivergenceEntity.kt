package br.com.grupokyly.apscoletor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import br.com.grupokyly.apscoletor.domain.model.SkipReason

@Entity(
    tableName = "divergences",
    foreignKeys = [
        ForeignKey(
            entity = BoxEntity::class,
            parentColumns = ["id"],
            childColumns = ["boxId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PickingItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["pickingItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("boxId"), Index("pickingItemId")]
)
data class DivergenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pickingItemId: Long,
    val boxId: Long,
    val barcode: String?,
    val reason: SkipReason,
    val registeredAt: Long,
    val syncedAt: Long? = null
)
