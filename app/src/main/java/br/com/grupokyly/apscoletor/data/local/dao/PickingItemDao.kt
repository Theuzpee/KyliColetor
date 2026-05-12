package br.com.grupokyly.apscoletor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.grupokyly.apscoletor.data.local.entity.PickingItemEntity
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PickingItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PickingItemEntity>): List<Long>

    @Query("UPDATE picking_items SET quantityCollected = quantityCollected + 1 WHERE id = :itemId")
    suspend fun incrementCollected(itemId: Long)

    @Query("UPDATE picking_items SET status = :status WHERE id = :itemId")
    suspend fun updateStatus(itemId: Long, status: ItemStatus)

    @Query("SELECT * FROM picking_items WHERE boxId = :boxId")
    fun getItemsByBox(boxId: Long): Flow<List<PickingItemEntity>>

    @Query("SELECT * FROM picking_items WHERE boxId = :boxId AND status = 'PENDENTE' ORDER BY id ASC LIMIT 1")
    suspend fun getNextPendingItem(boxId: Long): PickingItemEntity?

    @Query("SELECT * FROM picking_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): PickingItemEntity?

    @Query("SELECT * FROM picking_items WHERE boxId = :boxId")
    suspend fun getItemsByBoxId(boxId: Long): List<PickingItemEntity>
}
