package br.com.grupokyly.apscoletor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.grupokyly.apscoletor.domain.model.BoxStatus

import androidx.room.Index

@Entity(
    tableName = "boxes",
    indices = [
        Index(value = ["papeletaCode"], unique = true),
        Index(value = ["status"]),
        Index(value = ["syncedAt"])
    ]
)
data class BoxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val papeletaCode: String,
    val orderId: String,
    val status: BoxStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null
)
